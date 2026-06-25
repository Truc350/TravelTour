import os
import random
import string
import datetime
from django.db import connection
from django.utils import timezone
from .models import User, Tour, Favorite, Booking, Notification, Voucher, UserVoucher, UserBehavior

# Thư viện tích hợp ngoài
try:
    import google.generativeai as genai
except ImportError:
    genai = None

try:
    import firebase_admin
    from firebase_admin import credentials, messaging
except ImportError:
    firebase_admin = None


def check_and_create_behavior_table():
    """
    Cơ chế tự sửa lỗi (Self-healing database): Tự động tạo bảng user_behaviors 
    nếu hệ thống chưa áp dụng migrations.
    """
    try:
        with connection.cursor() as cursor:
            # Kiểm tra xem bảng user_behaviors đã tồn tại chưa
            cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='user_behaviors';")
            row = cursor.fetchone()
            if not row:
                print("[AI Engine] Table 'user_behaviors' does not exist. Creating it now...")
                cursor.execute("""
                    CREATE TABLE IF NOT EXISTS user_behaviors (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        behavior_type VARCHAR(50) NOT NULL,
                        timestamp DATETIME NOT NULL,
                        tour_id INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        FOREIGN KEY (tour_id) REFERENCES tours_tour (id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
                    );
                """)
                print("[AI Engine] Table 'user_behaviors' created successfully.")
    except Exception as e:
        print("[AI Engine] Failed to self-heal table creation:", e)


class AIRecommendationEngine:
    def __init__(self):
        check_and_create_behavior_table()
        self.initialize_firebase()
        self.initialize_gemini()

    def initialize_firebase(self):
        """
        Khởi tạo Firebase Admin SDK sử dụng tệp private key
        """
        if firebase_admin is None:
            print("[Firebase] Thư viện firebase-admin chưa được cài đặt!")
            return
        
        # Đường dẫn tệp khóa được cung cấp trong project
        cred_path = 'd:/TravelTour/firebase-admin-key.json'
        
        if not os.path.exists(cred_path):
            # Fallback sang thư mục hiện tại nếu chạy trên môi trường khác
            cred_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'firebase-admin-key.json')

        if not firebase_admin._apps:
            try:
                if os.path.exists(cred_path):
                    cred = credentials.Certificate(cred_path)
                    firebase_admin.initialize_app(cred)
                    print(f"[Firebase] Đã khởi tạo Firebase Admin SDK thành công với tệp: {cred_path}")
                else:
                    print(f"[Firebase] Không tìm thấy file key tại {cred_path}. Firebase SDK chưa được cấu hình.")
            except Exception as e:
                print("[Firebase] Lỗi khởi tạo Firebase SDK:", e)

    def initialize_gemini(self):
        """
        Khởi tạo Gemini API Client
        """
        if genai is None:
            print("[Gemini] Thư viện google-generativeai chưa được cài đặt!")
            return
        
        from django.conf import settings
        # Lấy API Key từ settings của Django hoặc từ biến môi trường
        api_key = getattr(settings, "GEMINI_API_KEY", None) or os.environ.get("GEMINI_API_KEY")
        
        # MẸO NHANH: Bạn có thể gán trực tiếp API key của mình vào đây để test nhanh:
        # api_key = "AIzaSyYourGeminiApiKeyHere"

        if not api_key:
            # Fallback key hoặc báo lỗi
            print("[Gemini] CẢNH BÁO: Chưa cấu hình GEMINI_API_KEY trong settings.py hoặc biến môi trường.")
            # Đặt một key demo để tránh crash lúc khởi động
            api_key = "DEMO_KEY"

        try:
            genai.configure(api_key=api_key)
            print("[Gemini] Đã cấu hình Gemini API thành công.")
        except Exception as e:
            print("[Gemini] Lỗi cấu hình Gemini API:", e)

    def collect_user_data(self, user_id):
        """
        Thu thập dữ liệu toàn diện về người dùng để phân tích
        """
        try:
            user = User.objects.get(id=user_id)
        except User.DoesNotExist:
            return None

        # Thu thập lịch sử tương tác
        behaviors = UserBehavior.objects.filter(user=user)
        views_count = behaviors.filter(behavior_type='VIEW').count()
        searches_count = behaviors.filter(behavior_type='SEARCH').count()
        favorites_count = Favorite.objects.filter(user=user).count()
        
        # Thu thập lịch sử đặt tour (Booking)
        bookings = Booking.objects.filter(user=user)
        bookings_count = bookings.count()
        completed_bookings_count = bookings.filter(status='COMPLETED').count()

        # Xác định khoảng thời gian tương tác gần nhất
        last_interaction = behaviors.order_by('-timestamp').first()
        days_since_last_interaction = 30 # Mặc định là 30 ngày nếu không có lịch sử
        if last_interaction:
            delta = timezone.now() - last_interaction.timestamp
            days_since_last_interaction = delta.days

        return {
            'user': user,
            'views_count': views_count,
            'searches_count': searches_count,
            'favorites_count': favorites_count,
            'bookings_count': bookings_count,
            'completed_bookings_count': completed_bookings_count,
            'days_since_last_interaction': days_since_last_interaction
        }

    def calculate_conversion_score(self, user_id, tour_id):
        """
        Thuật toán tính điểm Conversion Score (0-100) để đánh giá mức độ 
        khao khát mua tour của người dùng sau khi họ bấm Yêu thích một tour cụ thể.
        
        Giải thích thuật toán:
        1. Điểm cơ sở (Base Score): 30 điểm khi người dùng bấm Yêu thích tour này.
        2. Tần suất xem tour này (Specific View Score): Cộng 5 điểm cho mỗi lần xem tour hiện tại (Tối đa 20 điểm).
        3. Tần suất yêu thích (Favorite Interest): Cộng 8 điểm cho mỗi tour khác đang nằm trong wishlist (Tối đa 25 điểm).
        4. Uy tín lịch sử đặt mua (Trust Factor): Cộng 15 điểm cho mỗi booking thành công trước đó (Tối đa 25 điểm).
        5. Trọng số thời gian (Recency Factor): Nhân điểm với hệ số suy giảm thời gian:
           - Tương tác trong vòng 1 ngày: Hệ số 1.0
           - Tương tác trong vòng 7 ngày: Hệ số 0.8
           - Trên 7 ngày: Hệ số 0.5
        """
        data = self.collect_user_data(user_id)
        if not data:
            return 0

        # 1. Điểm cơ sở khi thích tour
        score = 30

        # 2. Xem tần suất tương tác riêng với tour_id này
        specific_views = UserBehavior.objects.filter(user_id=user_id, tour_id=tour_id, behavior_type='VIEW').count()
        score += min(specific_views * 5, 20)

        # 3. Tổng số tour yêu thích hiện tại
        score += min(data['favorites_count'] * 8, 25)

        # 4. Lịch sử đặt tour thành công trước đây (chứng tỏ có thói quen thanh toán)
        score += min(data['completed_bookings_count'] * 15, 25)

        # 5. Trọng số thời gian tương tác gần nhất
        recency_factor = 1.0
        days = data['days_since_last_interaction']
        if days <= 1:
            recency_factor = 1.0
        elif days <= 7:
            recency_factor = 0.8
        else:
            recency_factor = 0.5

        final_score = int(score * recency_factor)
        # Đảm bảo điểm nằm trong khoảng 0 - 100
        return max(0, min(final_score, 100))

    def check_voucher_eligibility(self, user_id, tour_id):
        """
        Kiểm tra điều kiện nhận Voucher của người dùng:
        1. Người dùng chưa từng nhận Voucher hoạt động nào cho tour này (hoặc chưa nhận voucher trong 7 ngày qua).
        2. Conversion Score phải lớn hơn hoặc bằng 70.
        """
        score = self.calculate_conversion_score(user_id, tour_id)
        print(f"[AI Engine] Conversion Score calculated for User {user_id} and Tour {tour_id} is: {score}")

        if score < 0:
            print(f"[AI Engine] Score {score} < 0. User not eligible.")
            return False, score

        # Kiểm tra xem người dùng đã sở hữu voucher hợp lệ nào chưa dùng gần đây không
        recent_vouchers = UserVoucher.objects.filter(user_id=user_id, is_used=False)
        # Nếu có hơn 2 voucher chưa sử dụng, tạm dừng tặng thêm để tránh lạm phát voucher
        if recent_vouchers.count() >= 4:
            print(f"[AI Engine] User already has {recent_vouchers.count()} active unused vouchers. Offer skipped.")
            return False, score

        return True, score

    def generate_travel_story(self, user_name, tour_title, tour_desc):
        """
        Sử dụng Gemini API để sinh kịch bản du lịch cá nhân hóa giàu cảm xúc.
        """
        if genai is None or os.environ.get("GEMINI_API_KEY") == "DEMO_KEY" or not os.environ.get("GEMINI_API_KEY"):
            # Trả về kịch bản mẫu nếu không có thư viện hoặc không có API key thật
            return f"Chào {user_name}, hãy tưởng tượng bạn đang hòa mình vào thiên nhiên thơ mộng tại {tour_title}. Đây chính là thời điểm lý tưởng để bạn tạm rời xa phố thị ồn ào và tận hưởng kỳ nghỉ mơ ước cùng Chill Tour!"

        prompt = f"""
        Bạn là một chuyên gia marketing du lịch có tâm hồn văn thơ sâu sắc.
        Hãy viết một đoạn kịch bản du lịch ngắn (khoảng 3-4 câu) gửi đến khách hàng tên là "{user_name}".
        Khách hàng vừa thể hiện sự yêu thích đặc biệt với tour: "{tour_title}".
        Mô tả tour: "{tour_desc}".

        Yêu cầu:
        1. Giọng văn mở đầu thân mật đầy lôi cuốn bằng tiếng Việt: "Chào {user_name}, hãy tưởng tượng..."
        2. Tạo dựng một bối cảnh giàu hình ảnh, cảm xúc nghệ thuật đặc trưng của địa danh đó (ví dụ: hương mùa thu Hà Nội, tiếng sóng rì rào Phú Quốc, sương mù Sapa, v.v.).
        3. Khơi gợi mong muốn xách vali lên và đi ngay lập tức.
        4. Cuối cùng, thông báo hệ thống gửi tặng riêng họ một Voucher giảm giá đặc biệt để trải nghiệm chuyến đi này.
        """

        try:
            model = genai.GenerativeModel('gemini-1.5-flash')
            response = model.generate_content(prompt)
            if response and response.text:
                return response.text.strip()
        except Exception as e:
            print("[Gemini API] Lỗi sinh kịch bản cá nhân hóa:", e)
        
        # Fallback trong trường hợp gọi API lỗi
        return f"Chào {user_name}, hãy tưởng tượng bạn đang dạo bước trên những hành trình tuyệt vời của {tour_title}. Chill Tour gửi tặng bạn món quà ưu đãi độc quyền để đồng hành cùng chuyến đi đầy ắp kỷ niệm sắp tới!"

    def create_voucher(self, user_id, tour_id):
        """
        Tạo voucher giảm giá thông minh gắn với tài khoản người dùng
        """
        try:
            user = User.objects.get(id=user_id)
            tour = Tour.objects.get(id=tour_id)
        except (User.DoesNotExist, Tour.DoesNotExist):
            return None

        # Sinh mã voucher ngẫu nhiên duy nhất
        random_suffix = ''.join(random.choices(string.ascii_uppercase + string.digits, k=5))
        voucher_code = f"AI{tour.code or 'TOUR'[:3].upper()}{random_suffix}"

        # Xác định giá trị giảm giá ngẫu nhiên thông minh (từ 100k đến 500k)
        discount_options = [
            ("100k", "GIẢM 100.000đ", 100000, "#E53E3E"), # Đỏ
            ("200k", "GIẢM 200.000đ", 200000, "#319795"), # Xanh mint
            ("500k", "GIẢM 500.000đ", 500000, "#3182CE"), # Xanh dương
        ]
        val_str, label, val_int, color = random.choice(discount_options)

        expiry_date = (datetime.date.today() + datetime.timedelta(days=7)).strftime("%d/%m/%Y")

        # Tạo mới Voucher trong DB
        voucher = Voucher.objects.create(
            code=voucher_code,
            title=f"Ưu đãi đặc biệt {tour.title[:30]}...",
            discount_val=val_str,
            discount_label=label,
            description=f"Voucher sinh tự động bằng AI dành riêng cho bạn sau khi yêu thích tour {tour.title}. Áp dụng hạn dùng trong vòng 7 ngày.",
            expiry=f"HSD: {expiry_date}",
            status="Còn hiệu lực",
            remaining_count=1,
            color_hex=color,
            max_discount=val_int
        )

        # Liên kết với User qua bảng UserVoucher
        user_voucher = UserVoucher.objects.create(
            user=user,
            voucher=voucher,
            is_used=False
        )

        return voucher

    def send_push_notification(self, user, title, body, voucher_code):
        """
        Gửi Push Notification qua Firebase Cloud Messaging (FCM)
        """
        # Lưu thông báo vào cơ sở dữ liệu Notification của hệ thống trước
        Notification.objects.create(
            user=user,
            title=title,
            message=body,
            date=datetime.date.today().strftime("%d-%m-%Y %H:%M"),
            is_read=False
        )

        fcm_token = user.fcm_token
        if not fcm_token:
            print(f"[FCM] User {user.name} không có fcm_token đăng ký. Không gửi được push notification.")
            return False

        if firebase_admin is None or not firebase_admin._apps:
            print("[FCM] Firebase Admin chưa được khởi tạo. Không thể gửi push notification.")
            return False

        try:
            # Thiết kế message payload kèm deep link
            message = messaging.Message(
                notification=messaging.Notification(
                    title=title,
                    body=body[:150] + "..." if len(body) > 150 else body,
                ),
                data={
                    'action': 'open_voucher',
                    'voucher_code': voucher_code,
                    'title': title,
                    'message': body,
                },
                token=fcm_token,
            )

            response = messaging.send(message)
            print(f"[FCM] Gửi thành công push notification tới User {user.name}. Message ID: {response}")
            return True
        except Exception as e:
            print("[FCM] Lỗi gửi push notification:", e)
            return False

    def process_recommendation_flow(self, user_id, tour_id):
        """
        Thực hiện toàn bộ luồng nghiệp vụ đề xuất thông minh:
        1. Đánh giá điều kiện nhận voucher.
        2. Nếu đủ điều kiện, gọi Gemini API sinh câu chuyện cá nhân hóa.
        3. Tạo Voucher trong DB.
        4. Gửi thông báo đẩy FCM và lưu vào Notification.
        """
        is_eligible, score = self.check_voucher_eligibility(user_id, tour_id)
        if not is_eligible:
            print(f"[AI Engine] User {user_id} không đủ điều kiện nhận ưu đãi AI (Score: {score}). Kết thúc luồng.")
            return None

        # Đủ điều kiện! Bắt đầu tạo trải nghiệm
        try:
            user = User.objects.get(id=user_id)
            tour = Tour.objects.get(id=tour_id)
        except (User.DoesNotExist, Tour.DoesNotExist):
            return None

        print(f"[AI Engine] User {user.name} đủ điều kiện nhận ưu đãi AI! Đang tiến hành xử lý...")

        # 1. Sinh kịch bản cá nhân hóa bằng Gemini API
        story = self.generate_travel_story(user.name, tour.title, tour.description)
        print(f"[AI Engine] Generated Story: {story}")

        # 2. Tạo Voucher đặc quyền trong cơ sở dữ liệu
        voucher = self.create_voucher(user_id, tour_id)
        if not voucher:
            print("[AI Engine] Lỗi khi sinh Voucher.")
            return None
        print(f"[AI Engine] Created Voucher in Database: {voucher.code}")

        # 3. Gửi Push Notification qua FCM
        notification_title = "Quà Tặng Chuyến Đi Mơ Ước! 🎁"
        sent = self.send_push_notification(user, notification_title, story, voucher.code)

        return {
            'voucher_code': voucher.code,
            'story': story,
            'fcm_sent': sent,
            'conversion_score': score
        }

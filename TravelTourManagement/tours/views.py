from django.db.models import Q, Case, When, IntegerField
from django.shortcuts import render, get_object_or_404
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework import generics, status
from rest_framework.response import Response
from .models import Tour, User, TourDeparture, Booking, Favorite, Notification, Passenger, TourImage, TourItinerary, Voucher, Review, UserVoucher
from .serializers import (
    TourSerializer,
    UserSerializer,
    TourDepartureSerializer,
    BookingSerializer,
    FavoriteSerializer,
    NotificationSerializer,
    PassengerSerializer,
    TourImageSerializer,
    TourItinerarySerializer,
    VoucherSerializer,
    ReviewSerializer,
    UserVoucherSerializer,
)


def remove_accents(text):
    if not text:
        return ""
    s1 = u'ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝàáâãèéêìíòóôõùúýĂăĐđĨĩŨũƠơƯưẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỶỷỸỹỴỵ'
    s0 = u'AAAAEEEIIOOOOUUYaaaaeeeiioooouuyAaDdIiUuOoUuAaAaAaAaAaAaAaAaAaAaAaAaEeEeEeEeEeEeEeEeIiIiOoOoOoOoOoOoOoOoOoOoOoOoUuUuUuUuUuUuUuYyYyYyYy'
    table = str.maketrans(s1, s0)
    return text.translate(table).lower()
def text_matches_keyword(all_text, keyword):
    """
    Kiểm tra keyword có khớp với text không.
    Tách keyword thành từng từ, tất cả từ đều phải xuất hiện trong text.
    Ví dụ: "hon son" → ["hon", "son"] → cả 2 phải có trong all_text
    """
    words = keyword.strip().split()
    return all(word in all_text for word in words)


class TourListAPIView(generics.ListAPIView):
    serializer_class = TourSerializer

    def get_queryset(self):
        queryset = Tour.objects.all()

        destination = self.request.query_params.get('destination', '').strip()
        origin = self.request.query_params.get('origin', '').strip()

        day_str = self.request.query_params.get('day', '').strip()
        month_str = self.request.query_params.get('month', '').strip()
        year_str = self.request.query_params.get('year', '').strip()

        has_date_filter = False
        possible_dates = []
        if day_str and month_str and year_str and day_str != '0':
            try:
                day = int(day_str)
                month = int(month_str) + 1
                year = int(year_str)
                has_date_filter = True
                possible_dates = [
                    f"{day:02d}-{month:02d}-{year}",
                    f"{day:02d}/{month:02d}/{year}",
                    f"{day}-{month}-{year}",
                    f"{day}/{month}/{year}"
                ]
            except ValueError:
                has_date_filter = False

        matching_date_ids = []
        other_ids = []

        normalized_dest = remove_accents(destination)
        normalized_origin = remove_accents(origin)

        for tour in queryset:
            # Gộp tất cả các trường text để tìm kiếm
            all_text = " ".join(remove_accents(x) for x in [
                tour.title or "",
                tour.description or "",
                tour.code or "",
                tour.provider or "",
            ])
            # 1. Lọc theo điểm đến
            if normalized_dest and normalized_dest != "ban muon di dau?":
                if not text_matches_keyword(all_text, normalized_dest):
                    continue
            # 2. Lọc theo điểm đi
            if normalized_origin and normalized_origin not in ["khoi hanh tu", "tat ca"]:
                if "ho chi minh" in normalized_origin or "hcm" in normalized_origin:
                    if not any(x in all_text for x in ["hcm", "ho chi minh"]):
                        continue
                elif "ha noi" in normalized_origin or "hn" in normalized_origin:
                    if not any(x in all_text for x in ["hn", "ha noi"]):
                        continue
                else:
                    if not text_matches_keyword(all_text, normalized_origin):
                        continue
            # 3. Phân loại theo ngày khởi hành
            if has_date_filter:
                has_departure = False
                for dep in tour.departures.all():
                    dep_date_str = str(dep.departure_date)
                    if any(pd in dep_date_str for pd in possible_dates):
                        has_departure = True
                        break
                if has_departure:
                    matching_date_ids.append(tour.id)
                else:
                    other_ids.append(tour.id)
            else:
                matching_date_ids.append(tour.id)

        ordered_ids = matching_date_ids + other_ids
        if ordered_ids:
            preserved = Case(
                *[When(id=pk, then=pos) for pos, pk in enumerate(ordered_ids)],
                output_field=IntegerField()
            )
            queryset = queryset.filter(id__in=ordered_ids).order_by(preserved)
        else:
            queryset = queryset.none()

        return queryset


class UserListCreateAPIView(generics.ListCreateAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer


class UserRetrieveUpdateDestroyAPIView(generics.RetrieveUpdateDestroyAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer


class TourDepartureListCreateAPIView(generics.ListCreateAPIView):
    queryset = TourDeparture.objects.all()
    serializer_class = TourDepartureSerializer


class TourDepartureRetrieveUpdateDestroyAPIView(generics.RetrieveUpdateDestroyAPIView):
    queryset = TourDeparture.objects.all()
    serializer_class = TourDepartureSerializer


class BookingListCreateAPIView(generics.ListCreateAPIView):
    queryset = Booking.objects.all()
    serializer_class = BookingSerializer

    def create(self, request, *args, **kwargs):
        data = request.data.copy()
        tour_id = data.get('tour_id')
        if tour_id:
            try:
                tour = Tour.objects.get(id=tour_id)
                departure = TourDeparture.objects.filter(tour=tour).first()
                if not departure:
                    departure = TourDeparture.objects.create(
                        tour=tour,
                        departure_date=data.get('booking_date', '2026-01-01'),
                        available_seats=14,
                        price=data.get('total_price', 0)
                    )
                data['departure'] = departure.id
            except Tour.DoesNotExist:
                pass
        
        serializer = self.get_serializer(data=data)
        serializer.is_valid(raise_exception=True)
        self.perform_create(serializer)

        # Mark UserVoucher as used and decrement count
        user_id = data.get('user')
        voucher_code = data.get('voucher_code')
        if user_id and voucher_code:
            try:
                voucher = Voucher.objects.filter(code=voucher_code).first()
                if voucher:
                    uv = UserVoucher.objects.filter(user_id=user_id, voucher=voucher).first()
                    if uv:
                        uv.is_used = True
                        uv.save()
                    else:
                        UserVoucher.objects.create(user_id=user_id, voucher=voucher, is_used=True)
                    if voucher.remaining_count > 0:
                        voucher.remaining_count -= 1
                        voucher.save()
            except Exception as e:
                print("Error updating UserVoucher on booking create:", e)

        # Send invoice email if requested
        booking = serializer.instance
        if booking.is_invoice_requested and booking.customer_email:
            try:
                import qrcode
                import io
                from django.core.mail import EmailMultiAlternatives
                from django.conf import settings

                tour_title = "Tour du lịch"
                tour_code = "TOUR"
                dep_date = str(booking.booking_date)
                dep_hour = booking.departure_hour or "08:00"
                if booking.departure:
                    if booking.departure.tour:
                        tour_title = booking.departure.tour.title
                        tour_code = booking.departure.tour.code or "TOUR"
                    if booking.departure.departure_date:
                        dep_date = str(booking.departure.departure_date)

                confirmation_code = f"CFM-{100000 + booking.id}"
                booking_code = f"DL0{booking.id}"

                # --- Build QR payload identical to Android app ---
                def remove_acc(text):
                    if not text:
                        return ""
                    import unicodedata
                    nfkd = unicodedata.normalize('NFD', text)
                    ascii_str = ''.join(c for c in nfkd if not unicodedata.combining(c))
                    return ascii_str.replace('đ', 'd').replace('Đ', 'D')

                qr_payload = (
                    "=== VE DIEN TU TRAVELTOUR ===\n"
                    f"Ma ve: {booking_code}\n"
                    f"Ma xac nhan: {confirmation_code}\n"
                    f"Tour: {remove_acc(tour_title)}\n"
                    f"Ngay khoi hanh: {dep_date}\n"
                    f"Gio khoi hanh: {dep_hour}\n"
                    f"Gia ve: {booking.total_price:,.0f} VND\n"
                    "Trang thai: Da thanh toan\n"
                    "============================"
                )

                # --- Generate QR code as CID inline attachment ---
                qr = qrcode.QRCode(version=1, error_correction=qrcode.constants.ERROR_CORRECT_M, box_size=8, border=4)
                qr.add_data(qr_payload)
                qr.make(fit=True)
                qr_img = qr.make_image(fill_color="#185FA5", back_color="white")
                buf = io.BytesIO()
                qr_img.save(buf, format='PNG')
                qr_image_data = buf.getvalue()

                # --- Passenger list HTML ---
                passengers = booking.passengers.all()
                passenger_rows = ""
                if passengers.exists():
                    for idx, p in enumerate(passengers, 1):
                        passenger_rows += f"""
                        <tr style="background:{'#F8FAFF' if idx % 2 == 0 else 'white'};">
                            <td style="padding:8px 12px;border-bottom:1px solid #E2E8F0;">{idx}</td>
                            <td style="padding:8px 12px;border-bottom:1px solid #E2E8F0;">{p.full_name or ''}</td>
                            <td style="padding:8px 12px;border-bottom:1px solid #E2E8F0;">{p.age or ''}</td>
                            <td style="padding:8px 12px;border-bottom:1px solid #E2E8F0;">{p.gender or ''}</td>
                        </tr>"""
                else:
                    passenger_rows = '<tr><td colspan="4" style="padding:12px;text-align:center;color:#718096;">Không có thông tin hành khách</td></tr>'

                subject = f"[Chill Tour] Hóa đơn điện tử - Đặt tour #{booking.id}"

                # --- Plain text fallback ---
                plain_text = (
                    f"Chào {booking.customer_name or 'Quý khách'},\n\n"
                    f"Cảm ơn bạn đã đặt tour tại Chill Tour!\n\n"
                    f"Mã đặt tour: {booking_code}\n"
                    f"Mã xác nhận: {confirmation_code}\n"
                    f"Tên tour: {tour_title}\n"
                    f"Ngày khởi hành: {dep_date}\n"
                    f"Giờ khởi hành: {dep_hour}\n"
                    f"Tổng tiền: {booking.total_price:,.0f} VND\n"
                    f"Trạng thái: Đã thanh toán thành công\n\n"
                    "Trân trọng, Đội ngũ Chill Tour."
                )

                # --- Rich HTML email body ---
                html_body = f"""<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Hóa đơn đặt tour</title>
</head>
<body style="margin:0;padding:0;background:#F0F4F8;font-family:'Segoe UI',Arial,sans-serif;">
<table width="100%" cellpadding="0" cellspacing="0" style="background:#F0F4F8;padding:32px 0;">
  <tr><td align="center">
    <table width="620" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.10);">

      <!-- Header -->
      <tr>
        <td style="background:linear-gradient(135deg,#185FA5 0%,#00B4D8 100%);padding:32px 40px;text-align:center;">
          <div style="font-size:28px;font-weight:800;color:#fff;letter-spacing:1px;">✈ Chill Tour</div>
          <div style="color:#BEE3F8;font-size:14px;margin-top:6px;">Hóa Đơn Điện Tử Đặt Tour</div>
        </td>
      </tr>

      <!-- Greeting -->
      <tr>
        <td style="padding:28px 40px 8px;">
          <p style="font-size:16px;color:#2D3748;margin:0;">
            Chào <strong>{booking.customer_name or 'Quý khách'}</strong>,
          </p>
          <p style="font-size:14px;color:#718096;margin:8px 0 0;">
            Cảm ơn bạn đã tin tưởng và đặt tour tại <strong>Chill Tour</strong>.
            Dưới đây là hóa đơn xác nhận đặt tour của bạn.
          </p>
        </td>
      </tr>

      <!-- Status Badge -->
      <tr>
        <td style="padding:16px 40px;">
          <div style="display:inline-block;background:#D1FAE5;color:#065F46;font-weight:700;font-size:13px;
                      padding:6px 18px;border-radius:20px;border:1px solid #6EE7B7;">
            ✓ ĐÃ THANH TOÁN THÀNH CÔNG
          </div>
        </td>
      </tr>

      <!-- Booking Info Card -->
      <tr>
        <td style="padding:0 40px 20px;">
          <table width="100%" cellpadding="0" cellspacing="0"
                 style="background:#F8FAFF;border-radius:12px;border:1px solid #BEE3F8;overflow:hidden;">
            <tr>
              <td colspan="2" style="background:#185FA5;padding:12px 20px;">
                <span style="color:#fff;font-weight:700;font-size:15px;">📋 THÔNG TIN ĐẶT TOUR</span>
              </td>
            </tr>
            <tr>
              <td style="padding:12px 20px;color:#718096;font-size:13px;width:45%;border-bottom:1px solid #E2E8F0;">Mã đặt tour</td>
              <td style="padding:12px 20px;color:#185FA5;font-weight:700;font-size:14px;border-bottom:1px solid #E2E8F0;">{booking_code}</td>
            </tr>
            <tr>
              <td style="padding:12px 20px;color:#718096;font-size:13px;border-bottom:1px solid #E2E8F0;background:#fff;">Mã xác nhận</td>
              <td style="padding:12px 20px;color:#2D3748;font-weight:600;font-size:13px;border-bottom:1px solid #E2E8F0;background:#fff;">{confirmation_code}</td>
            </tr>
            <tr>
              <td style="padding:12px 20px;color:#718096;font-size:13px;border-bottom:1px solid #E2E8F0;">Tên tour</td>
              <td style="padding:12px 20px;color:#2D3748;font-weight:600;font-size:13px;border-bottom:1px solid #E2E8F0;">{tour_title}</td>
            </tr>
            <tr>
              <td style="padding:12px 20px;color:#718096;font-size:13px;border-bottom:1px solid #E2E8F0;background:#fff;">Ngày khởi hành</td>
              <td style="padding:12px 20px;color:#2D3748;font-weight:600;font-size:13px;border-bottom:1px solid #E2E8F0;background:#fff;">📅 {dep_date}</td>
            </tr>
            <tr>
              <td style="padding:12px 20px;color:#718096;font-size:13px;border-bottom:1px solid #E2E8F0;">Giờ khởi hành</td>
              <td style="padding:12px 20px;color:#2D3748;font-weight:600;font-size:13px;border-bottom:1px solid #E2E8F0;">🕗 {dep_hour}</td>
            </tr>
          </table>
        </td>
      </tr>

      <!-- Customer Info -->
      <tr>
        <td style="padding:0 40px 20px;">
          <table width="100%" cellpadding="0" cellspacing="0"
                 style="background:#F8FAFF;border-radius:12px;border:1px solid #BEE3F8;overflow:hidden;">
            <tr>
              <td colspan="2" style="background:#185FA5;padding:12px 20px;">
                <span style="color:#fff;font-weight:700;font-size:15px;">👤 THÔNG TIN KHÁCH HÀNG</span>
              </td>
            </tr>
            <tr>
              <td style="padding:12px 20px;color:#718096;font-size:13px;width:45%;border-bottom:1px solid #E2E8F0;">Họ và tên</td>
              <td style="padding:12px 20px;color:#2D3748;font-weight:600;font-size:13px;border-bottom:1px solid #E2E8F0;">{booking.customer_name or ''}</td>
            </tr>
            <tr>
              <td style="padding:12px 20px;color:#718096;font-size:13px;border-bottom:1px solid #E2E8F0;background:#fff;">Số điện thoại</td>
              <td style="padding:12px 20px;color:#2D3748;font-weight:600;font-size:13px;border-bottom:1px solid #E2E8F0;background:#fff;">{booking.customer_phone or ''}</td>
            </tr>
            <tr>
              <td style="padding:12px 20px;color:#718096;font-size:13px;">Email nhận hóa đơn</td>
              <td style="padding:12px 20px;color:#185FA5;font-size:13px;">{booking.customer_email}</td>
            </tr>
          </table>
        </td>
      </tr>

      <!-- Passenger List -->
      <tr>
        <td style="padding:0 40px 20px;">
          <table width="100%" cellpadding="0" cellspacing="0"
                 style="border-radius:12px;border:1px solid #BEE3F8;overflow:hidden;">
            <tr>
              <td colspan="4" style="background:#185FA5;padding:12px 20px;">
                <span style="color:#fff;font-weight:700;font-size:15px;">🧳 DANH SÁCH HÀNH KHÁCH</span>
              </td>
            </tr>
            <tr style="background:#EBF4FF;">
              <th style="padding:10px 12px;text-align:left;font-size:12px;color:#4A5568;font-weight:600;border-bottom:1px solid #BEE3F8;">#</th>
              <th style="padding:10px 12px;text-align:left;font-size:12px;color:#4A5568;font-weight:600;border-bottom:1px solid #BEE3F8;">Họ và tên</th>
              <th style="padding:10px 12px;text-align:left;font-size:12px;color:#4A5568;font-weight:600;border-bottom:1px solid #BEE3F8;">Tuổi</th>
              <th style="padding:10px 12px;text-align:left;font-size:12px;color:#4A5568;font-weight:600;border-bottom:1px solid #BEE3F8;">Giới tính</th>
            </tr>
            {passenger_rows}
          </table>
        </td>
      </tr>

      <!-- Total Price -->
      <tr>
        <td style="padding:0 40px 24px;">
          <table width="100%" cellpadding="0" cellspacing="0"
                 style="background:linear-gradient(135deg,#185FA5,#00B4D8);border-radius:12px;">
            <tr>
              <td style="padding:20px 24px;">
                <div style="color:#BEE3F8;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:1px;">Tổng tiền thanh toán</div>
                <div style="color:#fff;font-size:28px;font-weight:800;margin-top:4px;">{booking.total_price:,.0f} <span style="font-size:16px;">VND</span></div>
              </td>
            </tr>
          </table>
        </td>
      </tr>

      <!-- QR Code -->
      <tr>
        <td style="padding:0 40px 32px;text-align:center;">
          <div style="background:#F8FAFF;border-radius:16px;border:2px dashed #BEE3F8;padding:24px;display:inline-block;">
            <div style="font-size:14px;color:#185FA5;font-weight:700;margin-bottom:12px;">🔲 Mã QR Vé Điện Tử</div>
            <img src="cid:qr_code_image" width="200" height="200" alt="QR Code vé điện tử"
                 style="display:block;margin:0 auto;border-radius:8px;"/>
            <div style="font-size:11px;color:#A0AEC0;margin-top:10px;">Quét mã QR để xem thông tin vé</div>
            <div style="font-size:12px;color:#4A5568;font-weight:600;margin-top:4px;">{confirmation_code}</div>
          </div>
        </td>
      </tr>

      <!-- Footer -->
      <tr>
        <td style="background:#F7FAFC;border-top:1px solid #E2E8F0;padding:24px 40px;text-align:center;">
          <p style="font-size:13px;color:#718096;margin:0 0 8px;">
            Hóa đơn này được phát hành tự động từ hệ thống <strong>Chill Tour</strong>.
          </p>
          <p style="font-size:13px;color:#718096;margin:0 0 4px;">
            Nếu cần hỗ trợ, vui lòng liên hệ: <a href="mailto:skydronevn.web@gmail.com" style="color:#185FA5;">skydronevn.web@gmail.com</a>
          </p>
          <p style="font-size:12px;color:#A0AEC0;margin:12px 0 0;">
            Chúc bạn có một chuyến đi vui vẻ và ý nghĩa! 🌟
          </p>
        </td>
      </tr>

    </table>
  </td></tr>
</table>
</body>
</html>"""

                email = EmailMultiAlternatives(
                    subject=subject,
                    body=plain_text,
                    from_email=settings.EMAIL_HOST_USER,
                    to=[booking.customer_email],
                )
                email.attach_alternative(html_body, "text/html")

                # Attach QR image inline with Content-ID
                from email.mime.image import MIMEImage
                qr_mime = MIMEImage(qr_image_data, _subtype='png')
                qr_mime.add_header('Content-ID', '<qr_code_image>')
                qr_mime.add_header('Content-Disposition', 'inline', filename='qr_code.png')
                email.attach(qr_mime)

                email.send(fail_silently=False)
                print(f"Sent HTML invoice email to {booking.customer_email} for booking #{booking.id}")
            except Exception as e:
                print(f"Failed to send invoice email: {e}")

        headers = self.get_success_headers(serializer.data)
        return Response(serializer.data, status=status.HTTP_201_CREATED, headers=headers)


class BookingRetrieveUpdateDestroyAPIView(generics.RetrieveUpdateDestroyAPIView):
    queryset = Booking.objects.all()
    serializer_class = BookingSerializer


class FavoriteListCreateAPIView(generics.ListCreateAPIView):
    queryset = Favorite.objects.all()
    serializer_class = FavoriteSerializer


class FavoriteRetrieveUpdateDestroyAPIView(generics.RetrieveUpdateDestroyAPIView):
    queryset = Favorite.objects.all()
    serializer_class = FavoriteSerializer


class NotificationListCreateAPIView(generics.ListCreateAPIView):
    queryset = Notification.objects.all()
    serializer_class = NotificationSerializer


class NotificationRetrieveUpdateDestroyAPIView(generics.RetrieveUpdateDestroyAPIView):
    queryset = Notification.objects.all()
    serializer_class = NotificationSerializer


class PassengerListCreateAPIView(generics.ListCreateAPIView):
    queryset = Passenger.objects.all()
    serializer_class = PassengerSerializer


class PassengerRetrieveUpdateDestroyAPIView(generics.RetrieveUpdateDestroyAPIView):
    queryset = Passenger.objects.all()
    serializer_class = PassengerSerializer


class TourImageListCreateAPIView(generics.ListCreateAPIView):
    queryset = TourImage.objects.all()
    serializer_class = TourImageSerializer


class TourImageRetrieveUpdateDestroyAPIView(generics.RetrieveUpdateDestroyAPIView):
    queryset = TourImage.objects.all()
    serializer_class = TourImageSerializer


class TourItineraryListCreateAPIView(generics.ListCreateAPIView):
    queryset = TourItinerary.objects.all()
    serializer_class = TourItinerarySerializer


class TourItineraryRetrieveUpdateDestroyAPIView(generics.RetrieveUpdateDestroyAPIView):
    queryset = TourItinerary.objects.all()
    serializer_class = TourItinerarySerializer


class VoucherListAPIView(generics.ListAPIView):
    serializer_class = VoucherSerializer

    def get_queryset(self):
        user_id = self.request.query_params.get('user_id')
        saved_only = self.request.query_params.get('saved_only')
        queryset = Voucher.objects.all()
        if user_id:
            if saved_only and saved_only.lower() == 'true':
                saved_voucher_ids = UserVoucher.objects.filter(user_id=user_id, is_used=False).values_list('voucher_id', flat=True)
                queryset = queryset.filter(id__in=saved_voucher_ids)
            else:
                used_voucher_ids = UserVoucher.objects.filter(user_id=user_id, is_used=True).values_list('voucher_id', flat=True)
                queryset = queryset.exclude(id__in=used_voucher_ids)
        return queryset


class UserVoucherListCreateAPIView(generics.ListCreateAPIView):
    queryset = UserVoucher.objects.all()
    serializer_class = UserVoucherSerializer

    def get_queryset(self):
        user_id = self.request.query_params.get('user_id')
        queryset = UserVoucher.objects.all()
        if user_id:
            queryset = queryset.filter(user_id=user_id)
        return queryset

    def create(self, request, *args, **kwargs):
        user_id = request.data.get('user')
        voucher_id = request.data.get('voucher')
        if not user_id or not voucher_id:
            return Response({"error": "user and voucher are required"}, status=status.HTTP_400_BAD_REQUEST)
        
        user_voucher, created = UserVoucher.objects.get_or_create(
            user_id=user_id,
            voucher_id=voucher_id
        )
        serializer = self.get_serializer(user_voucher)
        return Response(serializer.data, status=status.HTTP_201_CREATED if created else status.HTTP_200_OK)


class ReviewListCreateAPIView(generics.ListCreateAPIView):
    queryset = Review.objects.all()
    serializer_class = ReviewSerializer


@csrf_exempt
def ticket_verify_view(request, booking_id):
    """
    Hiển thị trang web xác nhận vé tour điện tử.
    - GET: Hiển thị thông tin vé và mã xác nhận.
    - POST: Cập nhật trạng thái booking thành COMPLETED (xác nhận chuyến đi).
    """
    booking = get_object_or_404(Booking, id=booking_id)

    if request.method == 'POST':
        booking.status = 'COMPLETED'
        booking.save()
        return JsonResponse({'success': True, 'message': 'Chuyến đi đã được xác nhận thành công!', 'status': 'COMPLETED'})

    # Lấy thông tin chi tiết
    departure = booking.departure
    tour = departure.tour if departure else None
    user = booking.user

    # Danh sách hành khách
    passengers = booking.passengers.all()

    # Mã xác nhận duy nhất
    confirmation_code = f"CFM-{100000 + booking.id}"

    # Trạng thái hiển thị
    status_map = {
        'PENDING': ('Chờ duyệt', '#F59E0B', '#FEF3C7'),
        'CONFIRMED': ('Đã thanh toán', '#3B82F6', '#DBEAFE'),
        'COMPLETED': ('Đã hoàn thành', '#10B981', '#D1FAE5'),
        'CANCELLED': ('Đã hủy', '#EF4444', '#FEE2E2'),
    }
    status_label, status_color, status_bg = status_map.get(booking.status, ('Không rõ', '#6B7280', '#F3F4F6'))

    context = {
        'booking': booking,
        'departure': departure,
        'tour': tour,
        'user': user,
        'passengers': passengers,
        'confirmation_code': confirmation_code,
        'status_label': status_label,
        'status_color': status_color,
        'status_bg': status_bg,
        'is_completed': booking.status == 'COMPLETED',
    }

    return render(request, 'ticket_verify.html', context)
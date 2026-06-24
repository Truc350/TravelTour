from django.db.models import Q, Case, When, IntegerField
from rest_framework import generics, status
from rest_framework.response import Response
from .models import Tour, User, TourDeparture, Booking, Favorite, Notification, Passenger, TourImage, TourItinerary, Voucher, Review
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
    queryset = Voucher.objects.all()
    serializer_class = VoucherSerializer


class ReviewListCreateAPIView(generics.ListCreateAPIView):
    queryset = Review.objects.all()
    serializer_class = ReviewSerializer
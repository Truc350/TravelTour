from rest_framework import serializers
from .models import Tour, User, TourDeparture, Booking, Favorite, Notification, Passenger, TourImage, TourItinerary, Voucher, Review, UserVoucher, UserBehavior

class ReviewSerializer(serializers.ModelSerializer):
    user_name = serializers.CharField(source='user.name', read_only=True)

    class Meta:
        model = Review
        fields = ['id', 'tour', 'user', 'user_name', 'rating', 'comment', 'created_at']


class TourImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = TourImage
        fields = '__all__'


class TourItinerarySerializer(serializers.ModelSerializer):
    class Meta:
        model = TourItinerary
        fields = '__all__'

class TourDepartureSimpleSerializer(serializers.ModelSerializer):
    class Meta:
        model = TourDeparture
        fields = '__all__'

class TourSerializer(serializers.ModelSerializer):
    images = TourImageSerializer(many=True, read_only=True)
    itineraries = TourItinerarySerializer(many=True, read_only=True)
    departures = TourDepartureSimpleSerializer(many=True, read_only=True)
    reviews = serializers.SerializerMethodField()

    class Meta:
        model = Tour
        fields = '__all__'

    def get_reviews(self, obj):
        # Sắp xếp các review từ mới nhất lên trên bằng Python để giữ cache prefetch
        reviews = sorted(obj.reviews.all(), key=lambda r: r.created_at, reverse=True)
        return ReviewSerializer(reviews, many=True).data



class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = '__all__'


class TourDepartureSerializer(serializers.ModelSerializer):
    tour_detail = TourSerializer(source='tour', read_only=True)

    class Meta:
        model = TourDeparture
        fields = '__all__'


class BookingSerializer(serializers.ModelSerializer):
    departure_detail = TourDepartureSerializer(source='departure', read_only=True)

    class Meta:
        model = Booking
        fields = '__all__'


class FavoriteSerializer(serializers.ModelSerializer):
    class Meta:
        model = Favorite
        fields = '__all__'


class NotificationSerializer(serializers.ModelSerializer):
    class Meta:
        model = Notification
        fields = '__all__'


class PassengerSerializer(serializers.ModelSerializer):
    class Meta:
        model = Passenger
        fields = '__all__'


class VoucherSerializer(serializers.ModelSerializer):
    is_saved = serializers.SerializerMethodField()
    is_used = serializers.SerializerMethodField()

    class Meta:
        model = Voucher
        fields = '__all__'

    def get_is_saved(self, obj):
        request = self.context.get('request')
        user_id = request.query_params.get('user_id') if request else None
        if user_id:
            return UserVoucher.objects.filter(user_id=user_id, voucher=obj).exists()
        return False

    def get_is_used(self, obj):
        request = self.context.get('request')
        user_id = request.query_params.get('user_id') if request else None
        if user_id:
            uv = UserVoucher.objects.filter(user_id=user_id, voucher=obj).first()
            if uv:
                return uv.is_used
        return False


class UserVoucherSerializer(serializers.ModelSerializer):
    voucher_detail = VoucherSerializer(source='voucher', read_only=True)

    class Meta:
        model = UserVoucher
        fields = '__all__'


class ReviewSerializer(serializers.ModelSerializer):
    user_name = serializers.CharField(source='user.name', read_only=True)

    class Meta:
        model = Review
        fields = ['id', 'tour', 'user', 'user_name', 'rating', 'comment', 'created_at']


class UserBehaviorSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserBehavior
        fields = '__all__'




from rest_framework import serializers
from .models import Tour, User, TourDeparture, Booking, Favorite, Notification, Passenger, TourImage, TourItinerary, Voucher, Review


class TourImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = TourImage
        fields = '__all__'


class TourItinerarySerializer(serializers.ModelSerializer):
    class Meta:
        model = TourItinerary
        fields = '__all__'


class TourSerializer(serializers.ModelSerializer):
    images = TourImageSerializer(many=True, read_only=True)
    itineraries = TourItinerarySerializer(many=True, read_only=True)
    reviews = serializers.SerializerMethodField()

    class Meta:
        model = Tour
        fields = '__all__'

    def get_reviews(self, obj):
        # Sắp xếp các review từ mới nhất lên trên
        reviews = obj.reviews.all().order_by('-created_at')
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
    class Meta:
        model = Voucher
        fields = '__all__'


class ReviewSerializer(serializers.ModelSerializer):
    user_name = serializers.CharField(source='user.name', read_only=True)

    class Meta:
        model = Review
        fields = ['id', 'tour', 'user', 'user_name', 'rating', 'comment', 'created_at']



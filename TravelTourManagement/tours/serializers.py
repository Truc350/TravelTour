from rest_framework import serializers
from .models import Tour, User, TourDeparture, Booking, Favorite, Notification, Passenger, TourImage, TourItinerary


class TourSerializer(serializers.ModelSerializer):
    class Meta:
        model = Tour
        fields = '__all__'


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = '__all__'


class TourDepartureSerializer(serializers.ModelSerializer):
    class Meta:
        model = TourDeparture
        fields = '__all__'


class BookingSerializer(serializers.ModelSerializer):
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


class TourImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = TourImage
        fields = '__all__'


class TourItinerarySerializer(serializers.ModelSerializer):
    class Meta:
        model = TourItinerary
        fields = '__all__'
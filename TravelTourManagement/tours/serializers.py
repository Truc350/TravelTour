from rest_framework import serializers
from .models import Tour, User, TourDeparture, Booking


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
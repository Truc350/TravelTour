from rest_framework import generics
from .models import Tour, User, TourDeparture, Booking, Favorite, Notification, Passenger, TourImage, TourItinerary
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
)


class TourListAPIView(generics.ListAPIView):
    queryset = Tour.objects.all()
    serializer_class = TourSerializer


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
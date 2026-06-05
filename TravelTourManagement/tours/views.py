from rest_framework import generics
from .models import Tour, User, TourDeparture, Booking, Favorite, Notification, Passenger
from .serializers import (
    TourSerializer,
    UserSerializer,
    TourDepartureSerializer,
    BookingSerializer,
    FavoriteSerializer,
    NotificationSerializer,
    PassengerSerializer,
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
from rest_framework import generics
from .models import Tour, User, TourDeparture, Booking
from .serializers import TourSerializer, UserSerializer, TourDepartureSerializer, BookingSerializer


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
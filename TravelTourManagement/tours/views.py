from rest_framework import generics
from .models import Tour, User
from .serializers import TourSerializer, UserSerializer


class TourListAPIView(generics.ListAPIView):
    queryset = Tour.objects.all()
    serializer_class = TourSerializer


class UserListCreateAPIView(generics.ListCreateAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer


class UserRetrieveUpdateDestroyAPIView(generics.RetrieveUpdateDestroyAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
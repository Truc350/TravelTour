from rest_framework import generics
from .models import Tour
from .serializers import TourSerializer


class TourListAPIView(generics.ListAPIView):
    queryset = Tour.objects.all()
    serializer_class = TourSerializer
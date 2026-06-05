from django.urls import path
from .views import (
    TourListAPIView,
    UserListCreateAPIView,
    UserRetrieveUpdateDestroyAPIView,
    TourDepartureListCreateAPIView,
    TourDepartureRetrieveUpdateDestroyAPIView,
    BookingListCreateAPIView,
    BookingRetrieveUpdateDestroyAPIView,
)

urlpatterns = [
    path('tours/', TourListAPIView.as_view(), name='tour-list'),
    path('users/', UserListCreateAPIView.as_view(), name='user-list-create'),
    path('users/<int:pk>/', UserRetrieveUpdateDestroyAPIView.as_view(), name='user-detail'),
    path('tour-departures/', TourDepartureListCreateAPIView.as_view(), name='tourdeparture-list-create'),
    path('tour-departures/<int:pk>/', TourDepartureRetrieveUpdateDestroyAPIView.as_view(), name='tourdeparture-detail'),
    path('bookings/', BookingListCreateAPIView.as_view(), name='booking-list-create'),
    path('bookings/<int:pk>/', BookingRetrieveUpdateDestroyAPIView.as_view(), name='booking-detail'),
]
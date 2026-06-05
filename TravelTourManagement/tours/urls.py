from django.urls import path
from .views import (
    TourListAPIView,
    UserListCreateAPIView,
    UserRetrieveUpdateDestroyAPIView,
    TourDepartureListCreateAPIView,
    TourDepartureRetrieveUpdateDestroyAPIView,
    BookingListCreateAPIView,
    BookingRetrieveUpdateDestroyAPIView,
    FavoriteListCreateAPIView,
    FavoriteRetrieveUpdateDestroyAPIView,
    NotificationListCreateAPIView,
    NotificationRetrieveUpdateDestroyAPIView,
    PassengerListCreateAPIView,
    PassengerRetrieveUpdateDestroyAPIView,
)

urlpatterns = [
    path('tours/', TourListAPIView.as_view(), name='tour-list'),
    path('users/', UserListCreateAPIView.as_view(), name='user-list-create'),
    path('users/<int:pk>/', UserRetrieveUpdateDestroyAPIView.as_view(), name='user-detail'),
    path('tour-departures/', TourDepartureListCreateAPIView.as_view(), name='tourdeparture-list-create'),
    path('tour-departures/<int:pk>/', TourDepartureRetrieveUpdateDestroyAPIView.as_view(), name='tourdeparture-detail'),
    path('bookings/', BookingListCreateAPIView.as_view(), name='booking-list-create'),
    path('bookings/<int:pk>/', BookingRetrieveUpdateDestroyAPIView.as_view(), name='booking-detail'),
    path('favorites/', FavoriteListCreateAPIView.as_view(), name='favorite-list-create'),
    path('favorites/<int:pk>/', FavoriteRetrieveUpdateDestroyAPIView.as_view(), name='favorite-detail'),
    path('notifications/', NotificationListCreateAPIView.as_view(), name='notification-list-create'),
    path('notifications/<int:pk>/', NotificationRetrieveUpdateDestroyAPIView.as_view(), name='notification-detail'),
    path('passengers/', PassengerListCreateAPIView.as_view(), name='passenger-list-create'),
    path('passengers/<int:pk>/', PassengerRetrieveUpdateDestroyAPIView.as_view(), name='passenger-detail'),
]
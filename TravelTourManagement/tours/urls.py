from django.urls import path
from .views import TourListAPIView, UserListCreateAPIView, UserRetrieveUpdateDestroyAPIView

urlpatterns = [
    path('tours/', TourListAPIView.as_view(), name='tour-list'),
    path('users/', UserListCreateAPIView.as_view(), name='user-list-create'),
    path('users/<int:pk>/', UserRetrieveUpdateDestroyAPIView.as_view(), name='user-detail'),
]
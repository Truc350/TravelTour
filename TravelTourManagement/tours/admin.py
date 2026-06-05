from django.contrib import admin
from .models import Tour, User, TourDeparture, Booking, Favorite, Notification, Passenger

admin.site.register(Tour)
admin.site.register(User)
admin.site.register(TourDeparture)
admin.site.register(Booking)
admin.site.register(Favorite)
admin.site.register(Notification)
admin.site.register(Passenger)
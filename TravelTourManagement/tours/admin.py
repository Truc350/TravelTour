from django.contrib import admin
from .models import Tour, User, TourDeparture, Booking

admin.site.register(Tour)
admin.site.register(User)
admin.site.register(TourDeparture)
admin.site.register(Booking)
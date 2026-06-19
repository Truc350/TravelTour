from django.contrib import admin
from import_export.admin import ImportExportModelAdmin
from .models import Tour, User, TourDeparture, Booking, Favorite, Notification, Passenger, TourImage, TourItinerary

@admin.register(Tour)
class TourAdmin(ImportExportModelAdmin):
    pass

@admin.register(User)
class UserAdmin(ImportExportModelAdmin):
    pass

@admin.register(TourDeparture)
class TourDepartureAdmin(ImportExportModelAdmin):
    pass

@admin.register(Booking)
class BookingAdmin(ImportExportModelAdmin):
    pass

@admin.register(Favorite)
class FavoriteAdmin(ImportExportModelAdmin):
    pass

@admin.register(Notification)
class NotificationAdmin(ImportExportModelAdmin):
    pass

@admin.register(Passenger)
class PassengerAdmin(ImportExportModelAdmin):
    pass

@admin.register(TourImage)
class TourImageAdmin(ImportExportModelAdmin):
    pass

@admin.register(TourItinerary)
class TourItineraryAdmin(ImportExportModelAdmin):
    pass
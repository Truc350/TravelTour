from django.db import models


class Tour(models.Model):
    code = models.CharField(max_length=50, unique=True)

    title = models.CharField(max_length=255)

    description = models.TextField()

    original_price = models.DecimalField(
        max_digits=12,
        decimal_places=2
    )

    discount_price = models.DecimalField(
        max_digits=12,
        decimal_places=2
    )

    provider = models.CharField(max_length=255)

    rating_score = models.FloatField(default=0)

    reviews_count = models.IntegerField(default=0)

    def __str__(self):
        return self.title


class User(models.Model):
    name = models.CharField(max_length=255)
    contact = models.CharField(max_length=255)
    password = models.CharField(max_length=255)
    avatar_url = models.CharField(max_length=255, blank=True, null=True)

    class Meta:
        db_table = 'users'

    def __str__(self):
        return self.name


class TourDeparture(models.Model):
    tour = models.ForeignKey(Tour, on_delete=models.CASCADE, related_name='departures')
    departure_date = models.CharField(max_length=255)
    available_seats = models.IntegerField()
    price = models.DecimalField(max_digits=12, decimal_places=2)

    class Meta:
        db_table = 'tour_departures'

    def __str__(self):
        return f"{self.tour.title} - {self.departure_date}"


class Booking(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='bookings')
    departure = models.ForeignKey(TourDeparture, on_delete=models.RESTRICT, related_name='bookings')
    booking_date = models.CharField(max_length=255)
    status = models.CharField(max_length=50, default='PENDING')
    total_price = models.DecimalField(max_digits=12, decimal_places=2)

    class Meta:
        db_table = 'bookings'

    def __str__(self):
        return f"Booking {self.id} by {self.user.name}"


class Favorite(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='favorites')
    tour = models.ForeignKey(Tour, on_delete=models.CASCADE, related_name='favorites')

    class Meta:
        db_table = 'favorites'

    def __str__(self):
        return f"{self.user.name} likes {self.tour.title}"


class Notification(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='notifications')
    title = models.CharField(max_length=255)
    message = models.TextField()
    date = models.CharField(max_length=255)
    is_read = models.BooleanField(default=False)

    class Meta:
        db_table = 'notifications'

    def __str__(self):
        return self.title


class Passenger(models.Model):
    booking = models.ForeignKey(Booking, on_delete=models.CASCADE, related_name='passengers')
    salutation = models.CharField(max_length=50)
    fullname = models.CharField(max_length=255)
    birthdate = models.CharField(max_length=255)
    nationality = models.CharField(max_length=255)
    issuing_country = models.CharField(max_length=255)
    expiry_date = models.CharField(max_length=255)
    id_or_passport = models.CharField(max_length=255)

    class Meta:
        db_table = 'passengers'

    def __str__(self):
        return self.fullname
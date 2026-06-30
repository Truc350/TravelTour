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

    description_tour_include = models.TextField(blank=True, null=True)

    note = models.TextField(blank=True, null=True)
    views = models.IntegerField(default=0)

    def __str__(self):
        return self.title


class User(models.Model):
    name = models.CharField(max_length=255)
    contact = models.CharField(max_length=255)
    password = models.CharField(max_length=255)
    avatar_url = models.CharField(max_length=255, blank=True, null=True)
    fcm_token = models.CharField(max_length=255, blank=True, null=True)
    class Meta:
        db_table = 'users'

    def __str__(self):
        return self.name


class TourDeparture(models.Model):
    tour = models.ForeignKey(Tour, on_delete=models.CASCADE, related_name='departures')
    departure_date = models.CharField(max_length=255)
    available_seats = models.IntegerField()
    price = models.DecimalField(max_digits=12, decimal_places=2)
    hour_departure = models.CharField(max_length=255, default="07:00, 17:00")

    class Meta:
        db_table = 'tour_departures'

    def __str__(self):
        return f"{self.tour.title} - {self.departure_date}"


class Booking(models.Model):
    STATUS_CHOICES = [
        ('PENDING', 'Chờ duyệt'),
        ('CONFIRMED', 'Đã xác nhận'),
        ('CANCELLED', 'Đã hủy'),
        ('COMPLETED', 'Đã hoàn thành'),
    ]
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='bookings')
    departure = models.ForeignKey(TourDeparture, on_delete=models.RESTRICT, related_name='bookings')
    booking_date = models.CharField(max_length=255)
    departure_hour = models.CharField(max_length=50, blank=True, null=True)
    status = models.CharField(max_length=50, choices=STATUS_CHOICES, default='PENDING')
    total_price = models.DecimalField(max_digits=12, decimal_places=2)
    voucher_code = models.CharField(max_length=50, blank=True, null=True)
    customer_name = models.CharField(max_length=255, blank=True, null=True)
    customer_phone = models.CharField(max_length=50, blank=True, null=True)
    customer_email = models.CharField(max_length=255, blank=True, null=True)
    is_invoice_requested = models.BooleanField(default=False)

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
    STATUS_CHOICES = [
        ('PENDING', 'Chờ duyệt'),
        ('VERIFIED', 'Đã duyệt'),
        ('REJECTED', 'Bị từ chối'),
    ]
    booking = models.ForeignKey(Booking, on_delete=models.CASCADE, related_name='passengers', null=True, blank=True)
    salutation = models.CharField(max_length=50)
    fullname = models.CharField(max_length=255)
    birthdate = models.CharField(max_length=255)
    nationality = models.CharField(max_length=255)
    issuing_country = models.CharField(max_length=255)
    expiry_date = models.CharField(max_length=255)
    id_or_passport = models.CharField(max_length=255)
    status = models.CharField(max_length=50, choices=STATUS_CHOICES, default='PENDING')

    class Meta:
        db_table = 'passengers'

    def __str__(self):
        return self.fullname


class TourImage(models.Model):
    tour = models.ForeignKey(Tour, on_delete=models.CASCADE, related_name='images')
    image_url = models.CharField(max_length=255)

    class Meta:
        db_table = 'tour_images'

    def __str__(self):
        return f"Image for {self.tour.title}"


class TourImageFeature(models.Model):
    tour_image = models.OneToOneField(TourImage, on_delete=models.CASCADE, related_name='feature')
    feature_data = models.TextField()  # JSON representation of feature dict

    class Meta:
        db_table = 'tour_image_features'

    def __str__(self):
        return f"Feature for TourImage #{self.tour_image.id}"



class TourItinerary(models.Model):
    tour = models.ForeignKey(Tour, on_delete=models.CASCADE, related_name='itineraries')
    day_number = models.IntegerField()
    title = models.CharField(max_length=255)
    description = models.TextField()

    class Meta:
        db_table = 'tour_itineraries'

    def __str__(self):
        return f"Day {self.day_number}: {self.title}"


class Voucher(models.Model):
    code = models.CharField(max_length=50, unique=True)
    title = models.CharField(max_length=255)
    discount_val = models.CharField(max_length=50)
    discount_label = models.CharField(max_length=50)
    description = models.TextField()
    expiry = models.CharField(max_length=100)
    status = models.CharField(max_length=50, default="Còn hiệu lực")
    remaining_count = models.IntegerField(default=100)
    color_hex = models.CharField(max_length=10, default="#319795")
    max_discount = models.IntegerField(default=0)

    class Meta:
        db_table = 'vouchers'

    def __str__(self):
        return f"{self.title} ({self.code})"


class UserVoucher(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='user_vouchers')
    voucher = models.ForeignKey(Voucher, on_delete=models.CASCADE, related_name='user_vouchers')
    is_used = models.BooleanField(default=False)
    saved_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'user_vouchers'
        unique_together = ('user', 'voucher')

    def __str__(self):
        return f"{self.user.name} - {self.voucher.code} (is_used={self.is_used})"



class Review(models.Model):
    tour = models.ForeignKey(Tour, on_delete=models.CASCADE, related_name='reviews')
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='reviews')
    rating = models.IntegerField()
    comment = models.TextField(blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'reviews'

    def __str__(self):
        return f"Review for {self.tour.title} by {self.user.name} ({self.rating} stars)"

    def save(self, *args, **kwargs):
        super().save(*args, **kwargs)
        # Update Tour rating_score and reviews_count
        tour = self.tour
        reviews = tour.reviews.all()
        tour.reviews_count = reviews.count()
        if tour.reviews_count > 0:
            total_rating = sum(r.rating for r in reviews)
            tour.rating_score = round(total_rating / tour.reviews_count, 1)
        else:
            tour.rating_score = 0
        tour.save()

    def delete(self, *args, **kwargs):
        tour = self.tour
        super().delete(*args, **kwargs)
        # Update Tour rating_score and reviews_count
        reviews = tour.reviews.all()
        tour.reviews_count = reviews.count()
        if tour.reviews_count > 0:
            total_rating = sum(r.rating for r in reviews)
            tour.rating_score = round(total_rating / tour.reviews_count, 1)
        else:
            tour.rating_score = 0
        tour.save()


class UserBehavior(models.Model):
    BEHAVIOR_CHOICES = [
        ('VIEW', 'Xem Tour'),
        ('SEARCH', 'Tìm kiếm'),
        ('FAVORITE', 'Yêu thích'),
        ('BOOK', 'Đặt Tour'),
    ]
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='behaviors', null=True, blank=True)
    tour = models.ForeignKey(Tour, on_delete=models.CASCADE, related_name='behaviors')
    behavior_type = models.CharField(max_length=50, choices=BEHAVIOR_CHOICES)
    timestamp = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = 'user_behaviors'

    def __str__(self):
        return f"{self.user.name} - {self.behavior_type} - {self.tour.title}"










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
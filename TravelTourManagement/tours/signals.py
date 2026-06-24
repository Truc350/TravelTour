from django.db.models.signals import pre_save, post_save
from django.dispatch import receiver
from .models import Booking
from tours.fcm_helper import send_push_notification
import logging

logger = logging.getLogger(__name__)

@receiver(pre_save, sender=Booking)
def booking_pre_save(sender, instance, **kwargs):
    if instance.id:
        try:
            previous = Booking.objects.get(id=instance.id)
            instance._previous_status = previous.status
        except Booking.DoesNotExist:
            instance._previous_status = None
    else:
        instance._previous_status = None

@receiver(post_save, sender=Booking)
def booking_post_save(sender, instance, created, **kwargs):
    previous_status = getattr(instance, '_previous_status', None)
    
    # Send push notification when a booking is created or updated to 'CONFIRMED'
    if instance.status == 'CONFIRMED':
        if created or previous_status != 'CONFIRMED':
            try:
                user = instance.user
                tour_title = instance.departure.tour.title
                title = "Đặt tour thành công! 🎉"
                message = f"Chuyến đi {tour_title} của bạn đã được xác nhận thanh toán."
                logger.info(f"Triggering FCM push for booking confirmation: booking_id={instance.id}, user={user.id}")
                send_push_notification(
                    user=user,
                    title=title,
                    message=message,
                    data={
                        "type": "booking_success",
                        "booking_id": str(instance.id),
                        "tour_title": tour_title,
                        "booking_date": instance.booking_date,
                    }
                )
            except Exception as e:
                logger.error(f"Error in booking post_save signal: {e}")

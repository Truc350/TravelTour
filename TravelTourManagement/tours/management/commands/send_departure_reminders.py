import logging
from django.core.management.base import BaseCommand
from datetime import datetime, timedelta
from tours.models import Booking
from tours.fcm_helper import send_push_notification

logger = logging.getLogger(__name__)

class Command(BaseCommand):
    help = 'Send daily push notifications for upcoming tour departures'

    def handle(self, *args, **options):
        self.stdout.write("Checking for upcoming tour departures...")
        
        # Find bookings starting tomorrow
        tomorrow = (datetime.now() + timedelta(days=1)).strftime("%Y-%m-%d")
        self.stdout.write(f"Looking for departures on date: {tomorrow}")
        
        # Get all confirmed bookings
        bookings = Booking.objects.filter(status='CONFIRMED')
        sent_count = 0
        
        for booking in bookings:
            dep_date = booking.departure.departure_date
            # Parse and check if date starts with tomorrow (YYYY-MM-DD)
            if dep_date and dep_date.startswith(tomorrow):
                user = booking.user
                tour_title = booking.departure.tour.title
                title = "Nhắc nhở khởi hành! 🎒"
                message = f"Tour du lịch '{tour_title}' của bạn sẽ khởi hành vào ngày mai ({tomorrow}). Hãy chuẩn bị sẵn sàng hành lý nhé!"
                
                self.stdout.write(f"Sending reminder to user {user.name} (ID: {user.id}) for tour '{tour_title}'")
                success = send_push_notification(
                    user=user,
                    title=title,
                    message=message,
                    data={
                        "type": "departure_reminder",
                        "booking_id": str(booking.id),
                        "tour_title": tour_title,
                        "departure_date": dep_date
                    }
                )
                if success:
                    sent_count += 1
        
        self.stdout.write(self.style.SUCCESS(f"Finished sending reminders. Total sent: {sent_count}"))

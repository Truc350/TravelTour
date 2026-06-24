import os
import logging
from django.conf import settings
from datetime import datetime
import firebase_admin
from firebase_admin import credentials, messaging
from tours.models import Notification

logger = logging.getLogger(__name__)

# Initialize Firebase Admin SDK
cred_path = os.path.join(settings.BASE_DIR, 'firebase-admin-key.json')
try:
    if not firebase_admin._apps:
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred)
        logger.info("Firebase Admin SDK initialized successfully.")
except Exception as e:
    logger.error(f"Error initializing Firebase Admin SDK: {e}")

def send_push_notification(user, title, message, data=None):
    """
    Sends a Firebase Cloud Messaging push notification to a user's fcm_token.
    Also creates a local Notification record in Django DB.
    """
    # 1. Save to local notifications DB first
    current_date = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    try:
        Notification.objects.create(
            user=user,
            title=title,
            message=message,
            date=current_date,
            is_read=False
        )
    except Exception as e:
        logger.error(f"Failed to create local Notification: {e}")

    # 2. Send FCM push if token is available
    if not user.fcm_token:
        logger.warning(f"User {user.id} has no fcm_token. Push skipped.")
        return False

    try:
        fcm_data = data or {}
        # Ensure all data values are strings
        string_data = {k: str(v) for k, v in fcm_data.items()}

        message_obj = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=message,
            ),
            data=string_data,
            token=user.fcm_token
        )
        response = messaging.send(message_obj)
        logger.info(f"FCM Notification sent successfully to user {user.id}: {response}")
        return True
    except Exception as e:
        logger.error(f"Error sending FCM notification to user {user.id}: {e}")
        return False

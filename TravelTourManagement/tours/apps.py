from django.apps import AppConfig


class ToursConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'tours'

    def ready(self):
        try:
            from django.core.management import call_command
            call_command('migrate', interactive=False)
            print("[ToursConfig] Đã tự động cập nhật Database (Migration) thành công!")
        except Exception as e:
            print("[ToursConfig] Lỗi khi tự động chạy migrate:", e)


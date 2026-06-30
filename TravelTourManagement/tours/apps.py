from django.apps import AppConfig


class ToursConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'tours'

    def ready(self):
        try:
            from django.core.management import call_command
            call_command('migrate', interactive=False)
            print("[ToursConfig] Database migration completed successfully.")
        except Exception as e:
            print("[ToursConfig] Error running automatic migrations: ", e)


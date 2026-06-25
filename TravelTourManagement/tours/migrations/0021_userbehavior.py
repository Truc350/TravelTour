# Generated manually on 2026-06-25

import django.db.models.deletion
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('tours', '0020_remove_tourdeparture_departure_hour_and_more'),
    ]

    operations = [
        migrations.CreateModel(
            name='UserBehavior',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('behavior_type', models.CharField(choices=[('VIEW', 'Xem Tour'), ('SEARCH', 'Tìm kiếm'), ('FAVORITE', 'Yêu thích'), ('BOOK', 'Đặt Tour')], max_length=50)),
                ('timestamp', models.DateTimeField(auto_now_add=True)),
                ('tour', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='behaviors', to='tours.tour')),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='behaviors', to='tours.user')),
            ],
            options={
                'db_table': 'user_behaviors',
            },
        ),
    ]

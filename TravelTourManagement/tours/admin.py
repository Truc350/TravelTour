from django.contrib import admin
from import_export import resources
from import_export.admin import ImportExportModelAdmin
from import_export.fields import Field
from import_export.widgets import DecimalWidget, ForeignKeyWidget
import re
from decimal import Decimal
from .models import Tour, User, TourDeparture, Booking, Favorite, Notification, Passenger, TourImage, TourItinerary, Voucher

class PriceWidget(DecimalWidget):
    def clean(self, value, row=None, *args, **kwargs):
        if value is None or str(value).strip() == '':
            return None
        # Clean currency formats like "5.290.000đ", "5,290,000", or "5290000.0"
        cleaned_value = re.sub(r'[^\d.,]', '', str(value))
        if '.' in cleaned_value and ',' in cleaned_value:
            cleaned_value = cleaned_value.replace('.', '').replace(',', '.')
        elif '.' in cleaned_value:
            parts = cleaned_value.split('.')
            # If multiple dots, or one dot with exactly 3 digits after it, it is a thousands separator.
            if len(parts) > 2 or (len(parts) == 2 and len(parts[1]) == 3 and float(parts[0]) > 0):
                cleaned_value = cleaned_value.replace('.', '')
        elif ',' in cleaned_value:
            cleaned_value = cleaned_value.replace(',', '')
            
        try:
            return Decimal(cleaned_value)
        except Exception:
            return super().clean(value, row, *args, **kwargs)

class CleanForeignKeyWidget(ForeignKeyWidget):
    def clean(self, value, row=None, *args, **kwargs):
        if value is None or str(value).strip() == '':
            return None
        cleaned_value = str(value).strip()
        try:
            return self.model.objects.get(**{self.field: cleaned_value})
        except self.model.DoesNotExist:
            try:
                return self.model.objects.get(**{f"{self.field}__iexact": cleaned_value})
            except self.model.DoesNotExist:
                # Quét và so sánh chuỗi đã loại bỏ khoảng trắng từ cả 2 phía để khớp dữ liệu bẩn trong DB
                for obj in self.model.objects.all():
                    db_val = getattr(obj, self.field)
                    if db_val and str(db_val).strip().lower() == cleaned_value.lower():
                        return obj
                raise

# Resources
class TourResource(resources.ModelResource):
    code = Field(attribute='code', column_name='tour_id')
    original_price = Field(attribute='original_price', column_name='original_price', widget=PriceWidget())
    discount_price = Field(attribute='discount_price', column_name='discount_price', widget=PriceWidget())

    class Meta:
        model = Tour
        import_id_fields = ('code',)
        fields = ('id', 'code', 'title', 'description', 'original_price', 'discount_price', 'provider', 'rating_score', 'reviews_count', 'description_tour_include', 'note')

    def before_import(self, dataset, **kwargs):
        # Chuẩn hóa tiêu đề cột sang chữ thường và loại bỏ khoảng trắng thừa
        dataset.headers = [str(h).strip().lower() for h in dataset.headers]
        
        # Kiểm tra tiêu đề nào có sẵn để thiết lập cột định danh code
        if 'tour_id' in dataset.headers:
            self.fields['code'].column_name = 'tour_id'
            self._meta.import_id_fields = ('code',)
        elif 'code' in dataset.headers:
            self.fields['code'].column_name = 'code'
            self._meta.import_id_fields = ('code',)
            
        if 'id' in dataset.headers:
            self._meta.import_id_fields = ('id',)

    def before_import_row(self, row, row_number=None, **kwargs):
        # Dọn dẹp khoảng trắng trước khi lưu code vào DB để giữ DB luôn sạch
        for key in list(row.keys()):
            if str(key).strip().lower() in ['tour_id', 'code']:
                if row[key] is not None:
                    row[key] = str(row[key]).strip()

class TourDepartureResource(resources.ModelResource):
    tour = Field(
        column_name='tour_id',
        attribute='tour',
        widget=CleanForeignKeyWidget(Tour, 'code')
    )
    price = Field(attribute='price', column_name='price', widget=PriceWidget())

    class Meta:
        model = TourDeparture
        fields = ('id', 'tour', 'departure_date', 'available_seats', 'price')

    def before_import(self, dataset, **kwargs):
        dataset.headers = [str(h).strip().lower() for h in dataset.headers]
        if 'tour_id' in dataset.headers:
            self.fields['tour'].column_name = 'tour_id'
        elif 'tour' in dataset.headers:
            self.fields['tour'].column_name = 'tour'
            
        if 'id' in dataset.headers:
            self._meta.import_id_fields = ('id',)
        else:
            # Nếu không có cột ID, định danh dòng dựa trên tổ hợp Tour và ngày khởi hành
            self._meta.import_id_fields = ('tour', 'departure_date')

class TourImageResource(resources.ModelResource):
    tour = Field(
        column_name='tour_id',
        attribute='tour',
        widget=CleanForeignKeyWidget(Tour, 'code')
    )

    class Meta:
        model = TourImage
        fields = ('id', 'tour', 'image_url')

    def before_import(self, dataset, **kwargs):
        dataset.headers = [str(h).strip().lower() for h in dataset.headers]
        if 'tour_id' in dataset.headers:
            self.fields['tour'].column_name = 'tour_id'
        elif 'tour' in dataset.headers:
            self.fields['tour'].column_name = 'tour'
            
        if 'id' in dataset.headers:
            self._meta.import_id_fields = ('id',)
        else:
            # Nếu không có cột ID, định danh dòng dựa trên tour và đường dẫn ảnh
            self._meta.import_id_fields = ('tour', 'image_url')

class TourItineraryResource(resources.ModelResource):
    tour = Field(
        column_name='tour_id',
        attribute='tour',
        widget=CleanForeignKeyWidget(Tour, 'code')
    )

    class Meta:
        model = TourItinerary
        fields = ('id', 'tour', 'day_number', 'title', 'description')

    def before_import(self, dataset, **kwargs):
        dataset.headers = [str(h).strip().lower() for h in dataset.headers]
        if 'tour_id' in dataset.headers:
            self.fields['tour'].column_name = 'tour_id'
        elif 'tour' in dataset.headers:
            self.fields['tour'].column_name = 'tour'
            
        if 'id' in dataset.headers:
            self._meta.import_id_fields = ('id',)
        else:
            # Nếu không có cột ID, định danh dòng dựa trên tour và số thứ tự ngày đi
            self._meta.import_id_fields = ('tour', 'day_number')

# Admin Configurations
@admin.register(Tour)
class TourAdmin(ImportExportModelAdmin):
    resource_classes = [TourResource]

@admin.register(User)
class UserAdmin(ImportExportModelAdmin):
    list_display = ('id', 'name', 'contact', 'avatar_url')
    search_fields = ('name', 'contact')

@admin.register(TourDeparture)
class TourDepartureAdmin(ImportExportModelAdmin):
    resource_classes = [TourDepartureResource]

@admin.register(Booking)
class BookingAdmin(ImportExportModelAdmin):
    list_display = ('id', 'user', 'departure', 'booking_date', 'departure_hour', 'status', 'total_price')
    list_filter = ('status', 'booking_date')
    search_fields = ('user__name', 'departure__tour__title')
    list_editable = ('status',)
    actions = ['confirm_bookings', 'cancel_bookings']

    def confirm_bookings(self, request, queryset):
        rows_updated = queryset.update(status='CONFIRMED')
        self.message_user(request, f"Đã xác nhận {rows_updated} bookings thành công.")
    confirm_bookings.short_description = "Xác nhận các Booking đã chọn"

    def cancel_bookings(self, request, queryset):
        rows_updated = queryset.update(status='CANCELLED')
        self.message_user(request, f"Đã hủy {rows_updated} bookings thành công.")
    cancel_bookings.short_description = "Hủy các Booking đã chọn"

@admin.register(Favorite)
class FavoriteAdmin(ImportExportModelAdmin):
    list_display = ('id', 'user', 'tour')

@admin.register(Notification)
class NotificationAdmin(ImportExportModelAdmin):
    list_display = ('id', 'user', 'title', 'date', 'is_read')
    list_filter = ('is_read', 'date')

@admin.register(Passenger)
class PassengerAdmin(ImportExportModelAdmin):
    list_display = ('id', 'salutation', 'fullname', 'birthdate', 'nationality', 'id_or_passport', 'status', 'booking')
    list_filter = ('status', 'nationality', 'salutation')
    search_fields = ('fullname', 'id_or_passport')
    list_editable = ('status',)
    actions = ['verify_passengers', 'reject_passengers']

    def verify_passengers(self, request, queryset):
        rows_updated = queryset.update(status='VERIFIED')
        self.message_user(request, f"Đã duyệt {rows_updated} hành khách thành công.")
    verify_passengers.short_description = "Duyệt thông tin các hành khách đã chọn"

    def reject_passengers(self, request, queryset):
        rows_updated = queryset.update(status='REJECTED')
        self.message_user(request, f"Đã từ chối {rows_updated} hành khách.")
    reject_passengers.short_description = "Từ chối thông tin các hành khách đã chọn"

@admin.register(TourImage)
class TourImageAdmin(ImportExportModelAdmin):
    resource_classes = [TourImageResource]

@admin.register(TourItinerary)
class TourItineraryAdmin(ImportExportModelAdmin):
    resource_classes = [TourItineraryResource]


@admin.register(Voucher)
class VoucherAdmin(ImportExportModelAdmin):
    list_display = ('id', 'code', 'title', 'discount_val', 'discount_label', 'expiry', 'status', 'remaining_count')
    search_fields = ('code', 'title')
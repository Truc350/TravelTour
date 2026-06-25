import json
from django.core.management.base import BaseCommand
from tours.models import TourImage, TourImageFeature
from tours.utils import download_image, extract_features

class Command(BaseCommand):
    help = 'Precomputes visual feature descriptors for all TourImage objects in the database'

    def handle(self, *args, **options):
        images = TourImage.objects.all()
        total = images.count()
        self.stdout.write(self.style.SUCCESS(f"Found {total} images to process."))

        processed_count = 0
        skipped_count = 0
        error_count = 0

        for idx, img in enumerate(images, 1):
            # Check if features are already computed
            if hasattr(img, 'feature'):
                skipped_count += 1
                continue

            self.stdout.write(f"[{idx}/{total}] Processing image ID {img.id} (URL: {img.image_url})...")
            
            # Download image
            pil_img = download_image(img.image_url)
            if pil_img is None:
                self.stdout.write(self.style.WARNING(f"Failed to download image ID {img.id}. Skipping..."))
                error_count += 1
                continue

            # Extract features
            features = extract_features(pil_img)
            if features is None:
                self.stdout.write(self.style.WARNING(f"Failed to extract features for image ID {img.id}. Skipping..."))
                error_count += 1
                continue

            # Save to database
            TourImageFeature.objects.create(
                tour_image=img,
                feature_data=json.dumps(features)
            )
            processed_count += 1

        self.stdout.write(self.style.SUCCESS(
            f"Finished processing! Success: {processed_count}, Skipped (already existed): {skipped_count}, Errors: {error_count}"
        ))

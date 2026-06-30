from django.core.management.base import BaseCommand
from tours.chatbot.rag_engine import TourRAGEngine
import time

class Command(BaseCommand):
    help = "Build or rebuild the FAISS vector index for Tours semantic search using BAAI/bge-m3"

    def handle(self, *args, **options):
        self.stdout.write(self.style.WARNING("Starting TourRAGEngine initialization..."))
        start_time = time.time()
        
        try:
            # Khởi tạo engine (Sẽ tải model BAAI/bge-m3)
            engine = TourRAGEngine()
            
            self.stdout.write(self.style.WARNING("Encoding tours data and building FAISS Index... (This might take a few minutes on first run)"))
            success = engine.build_index()
            
            elapsed = time.time() - start_time
            if success:
                self.stdout.write(
                    self.style.SUCCESS(f"Success! FAISS Index has been built successfully in {elapsed:.2f} seconds.")
                )
            else:
                self.stdout.write(
                    self.style.ERROR("Failed to build FAISS Index. No tours data found.")
                )
                
        except Exception as e:
            self.stdout.write(
                self.style.ERROR(f"Fatal error during build index: {str(e)}")
            )

import requests
import numpy as np
from PIL import Image
from io import BytesIO

# Lazy-loaded imports and variables to avoid slow startup for other views
_model = None
_preprocess = None

def _get_model_and_transforms():
    """
    Lazy loads the MobileNetV2 model and torchvision transforms.
    """
    global _model, _preprocess
    if _model is None:
        import torch
        import torchvision.models as models
        import torchvision.transforms as transforms
        
        # Load pre-trained MobileNetV2
        try:
            from torchvision.models import mobilenet_v2, MobileNet_V2_Weights
            _model = mobilenet_v2(weights=MobileNet_V2_Weights.DEFAULT)
        except ImportError:
            _model = models.mobilenet_v2(pretrained=True)
        
        # Replace classifier with Identity to output the 1280-dimensional feature vector
        _model.classifier = torch.nn.Identity()
        _model.eval()
        
        _preprocess = transforms.Compose([
            transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
        ])
    return _model, _preprocess

def download_image(url):
    """
    Downloads an image from a URL and returns a PIL Image.
    Uses browser User-Agent to avoid getting blocked.
    """
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, prefix) Chrome/110.0.0.0 Safari/537.36'
        }
        response = requests.get(url, headers=headers, timeout=10)
        if response.status_code == 200:
            return Image.open(BytesIO(response.content))
        else:
            print(f"Failed to download image {url}, HTTP status {response.status_code}")
    except Exception as e:
        print(f"Error downloading image {url}: {e}")
    return None

def extract_features(img):
    """
    Extracts semantic features from a PIL Image using MobileNetV2.
    Returns a 1280-dimensional list of floats.
    """
    if img is None:
        return None
    try:
        import torch
        if img.mode != 'RGB':
            img = img.convert('RGB')
        
        model, preprocess = _get_model_and_transforms()
        
        # Preprocess and add batch dimension
        input_tensor = preprocess(img).unsqueeze(0)
        
        with torch.no_grad():
            # Get 1280-dimensional embedding
            features = model(input_tensor)
            feature_vector = features.squeeze(0).numpy().tolist()
            
        return feature_vector
    except Exception as e:
        print(f"Error extracting features with MobileNetV2: {e}")
        return None

def calculate_similarity(feat1, feat2):
    """
    Calculates the Cosine Similarity between two feature vectors.
    Returns a float in [0.0, 1.0].
    """
    if not feat1 or not feat2:
        return 0.0
    try:
        u = np.array(feat1, dtype=np.float32)
        v = np.array(feat2, dtype=np.float32)
        
        dot_product = np.dot(u, v)
        norm_u = np.linalg.norm(u)
        norm_v = np.linalg.norm(v)
        
        if norm_u == 0.0 or norm_v == 0.0:
            return 0.0
            
        cosine_sim = dot_product / (norm_u * norm_v)
        # Clip to [0.0, 1.0] range
        score = float(np.clip(cosine_sim, 0.0, 1.0))
        return score
    except Exception as e:
        print(f"Error calculating Cosine Similarity: {e}")
        return 0.0

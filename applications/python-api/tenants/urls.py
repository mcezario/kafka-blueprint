from django.urls import path
from .views import create_tenant

urlpatterns = [
    path('create/', create_tenant, name='create_tenant'),
]
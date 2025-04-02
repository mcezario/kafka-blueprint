# Use the official Python image
FROM python:3.11

# Set the working directory inside the container
WORKDIR /app

# Copy the requirements file and install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the entire Django project
COPY . .

# Expose the application port
EXPOSE 8000

# Run migrations and start Django
CMD ["gunicorn", "--bind", "0.0.0.0:8000", "tenant_project.wsgi:application"]
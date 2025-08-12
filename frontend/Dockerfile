# Stage 1: Build with Vite
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files and install
COPY package*.json ./
RUN npm install

# Clean problematic native bindings
RUN rm -rf node_modules package-lock.json && npm install

# Copy remaining files
COPY . .

# Build the project
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

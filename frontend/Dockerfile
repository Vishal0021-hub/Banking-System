# =============================================================
#  Frontend — React 19 / Vite / Tailwind CSS
#  Build context: frontend/ directory
#  docker build -t bankops/frontend ./frontend
#
#  Stage 1: Node.js builds the production assets (vite build)
#  Stage 2: Nginx serves the static files — tiny final image
# =============================================================

# ---- Stage 1: Build React app ----
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first (layer cache: only re-install if package.json changed)
COPY package.json package-lock.json ./

RUN npm ci --prefer-offline

# Copy source and build
COPY . .

# VITE_API_BASE_URL is set for relative API calls through the nginx reverse proxy
ENV VITE_API_BASE_URL="/api/v1"

RUN npm run build

# ---- Stage 2: Serve with nginx ----
FROM nginx:1.27-alpine

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom nginx config
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf

# Copy built React assets from Stage 1
COPY --from=builder /app/dist /usr/share/nginx/html

# nginx runs as root by default on alpine — acceptable for internal container
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

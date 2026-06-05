# K-Means Clustering — Concurrent & Parallel Benchmarking System

A full-stack benchmarking system that compares three implementations of the K-Means clustering algorithm: **Sequential**, **Concurrent** (manual threads + CyclicBarrier), and **Parallel** (ExecutorService thread pool). The system exposes a REST API backed by Spring Boot and a React dashboard for interactive benchmarking and visualisation.

---

## Table of Contents

- [System Architecture](#system-architecture)
- [Algorithms](#algorithms)
- [Prerequisites](#prerequisites)
- [Running the Application](#running-the-application)
  - [1. Backend](#1-backend)
  - [2. Frontend](#2-frontend)
- [Offline Benchmarking (CLI)](#offline-benchmarking-cli)
- [API Reference](#api-reference)
- [Datasets](#datasets)
- [Project Structure](#project-structure)

---

## System Architecture

```
┌─────────────────────────────────┐        ┌──────────────────────────────────┐
│   Frontend  (React + Vite)      │        │   Backend  (Spring Boot)         │
│   localhost:5173                │──/api──▶│   localhost:8080                 │
│                                 │        │                                  │
│  • Interactive dashboard        │        │  • REST controllers               │
│  • Thread / dataset config      │        │  • KMeansClusteringService        │
│  • Scatter, bar, and line charts│        │  • Sequential / Concurrent /      │
│  • CSV export                   │        │    Parallel implementations       │
└─────────────────────────────────┘        └──────────────────────────────────┘
```

All `/api/*` requests from the frontend are proxied to the backend automatically during development — no CORS configuration is needed.

---

## Algorithms

| Name | Class | Strategy |
|---|---|---|
| **Sequential** | `KMeansSequential` | Single-threaded Lloyd's algorithm |
| **Concurrent** | `KMeansConcurrent` | Manual `Thread` creation with `CyclicBarrier` synchronisation |
| **Parallel** | `KMeansParallel` | Fixed `ExecutorService` thread pool with `Future`-based task submission |

All three return a `KMeansCore.Result` containing: `centroids`, `labels`, `iterations`, and `inertia`.

---

## Prerequisites

| Tool | Version |
|---|---|
| Java (JDK) | 17 or later |
| Node.js | 18 or later |
| npm | 9 or later |

> The backend uses the Gradle wrapper (`gradlew`) — you do **not** need to install Gradle separately.

---

## Running the Application

### 1. Backend

Open a terminal in the `backend/` directory.

```bash
# Windows
.\gradlew.bat bootRun

# macOS / Linux
./gradlew bootRun
```

The Spring Boot server starts on **http://localhost:8080**.

To run the test suite:

```bash
# Windows
.\gradlew.bat test

# macOS / Linux
./gradlew test
```

---

### 2. Frontend

Open a **second** terminal in the `frontend/` directory.

```bash
# Install dependencies (first time only)
npm install

# Start the development server
npm run dev
```

Open **http://localhost:5173** in your browser.

The Vite dev server proxies all `/api` calls to `http://localhost:8080`, so both servers must be running at the same time.

To build a production bundle:

```bash
npm run build       # outputs to frontend/dist/
npm run preview     # serve the built bundle locally
```

---

## Offline Benchmarking (CLI)

`MainApp` runs all algorithms across every dataset in the `datasets/` folder and prints a formatted results table to the console. It does not require the frontend.

Run it from the `backend/` directory:

```bash
# Windows
.\gradlew.bat run -PmainClass=com.example.backend.experiment.MainApp

# macOS / Linux
./gradlew run -PmainClass=com.example.backend.experiment.MainApp
```

Alternatively, run it directly from your IDE by executing `com.example.backend.experiment.MainApp`.

**Output columns:** Dataset · Algorithm · Threads · Time (ms) · Mem (MB) · Iterations

Thread counts tested: **1, 2, 4, 8**

---

## API Reference

All endpoints accept and return JSON.

### Clustering

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/kmeans/sequential` | Run sequential K-Means |
| `POST` | `/api/kmeans/concurrent` | Run concurrent K-Means |
| `POST` | `/api/kmeans/parallel` | Run parallel K-Means |

**Request body:**

```json
{
  "datasetFilePath": "../datasets/dataset_10000points_3D_K5.csv",
  "k": 5,
  "threads": 4,
  "maxIterations": 300,
  "tolerance": 1e-6,
  "seed": 42
}
```

`maxIterations`, `tolerance`, and `seed` are optional (defaults: `300`, `1e-6`, `0`).

**Response body:**

```json
{
  "centroids": [[...], ...],
  "labels": [0, 2, 1, ...],
  "iterations": 47,
  "inertia": 123456.78,
  "executionTimeMs": 312,
  "numPoints": 10000,
  "numDimensions": 3
}
```

### Datasets

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/datasets/load` | Load a CSV file from disk |
| `POST` | `/api/datasets/generate` | Generate a synthetic clustered dataset |
| `POST` | `/api/datasets/preprocess` | Preprocess a raw Kaggle CSV into RFM format |

---

## Datasets

Datasets live in the `datasets/` folder at the project root.

### Synthetic datasets

Generated by `DatasetGenerator` with well-separated Gaussian clusters.

**Naming convention:** `dataset_[POINTS]points_[DIMS]D_K[K].csv`

| Points | Dimensions | K values |
|---|---|---|
| 10,000 | 3D, 5D, 10D | K=5, K=10 |
| 100,000 | 3D, 5D, 10D | K=5, K=10 |
| 1,000,000 | 3D, 5D, 10D | K=5, K=10 |

### Real-world dataset

| File | Description |
|---|---|
| `walmart_rfm_clean.csv` | Walmart customer transactions preprocessed into RFM (Recency, Frequency, Monetary) features, K=5 segments |

To regenerate the synthetic datasets, run `DatasetMain`:

```bash
# Windows
.\gradlew.bat run -PmainClass=com.example.backend.dataset.DatasetMain
```

---

## Project Structure

```
KMeansClustering/
├── backend/                          Spring Boot application
│   └── src/main/java/com/example/backend/
│       ├── clustering/
│       │   ├── core/                 KMeansCore (utilities, Result record)
│       │   ├── sequential/           KMeansSequential
│       │   ├── concurrent/           KMeansConcurrent
│       │   └── parallel/             KMeansParallel
│       ├── dataset/                  CSV loading, generation, preprocessing
│       ├── experiment/               MainApp (offline benchmarking CLI)
│       ├── service/                  KMeansClusteringService
│       ├── web/                      REST controllers and DTOs
│       └── utils/                    Error handling
│
├── frontend/                         React + Vite dashboard
│   └── src/
│       ├── api/                      HTTP client
│       ├── components/               UI components and charts
│       ├── hooks/                    Benchmark execution hooks
│       ├── state/                    Reducer-based state management
│       └── lib/                      Utilities (export, colours, sampling)
│
└── datasets/                         CSV datasets (synthetic + real-world)
```

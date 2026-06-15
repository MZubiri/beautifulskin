import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Producto {
  id?: number;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  idCategoria: number;
  stockMinimo?: number;
  codigoBarras?: string;
  imagenUrl?: string;
}

export interface ImageUploadResponse {
  url: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private http = inject(HttpClient);
  private apiUrl = '/api/productos';

  getAllProducts(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl);
  }

  getProductById(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/${id}`);
  }

  createProduct(product: Producto): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, product);
  }

  updateProduct(id: number, product: Producto): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, product);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getProductByBarcode(barcode: string): Observable<Producto> {
    return this.http.get<Producto>(`${this.apiUrl}/barras/${barcode}`);
  }

  getLowStockProducts(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/alertas`);
  }

  uploadProductImage(file: File): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ImageUploadResponse>(`${this.apiUrl}/imagenes`, formData);
  }

  getPublicImageUrl(path: string): string {
    return path;
  }
}

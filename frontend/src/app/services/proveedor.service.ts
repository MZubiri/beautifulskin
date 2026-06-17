import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Proveedor {
  id?: number;
  nombre: string;
  ruc: string;
  direccion?: string;
  telefono?: string;
  email?: string;
}

export interface OrdenCompra {
  id?: number;
  idProveedor: number;
  fechaCreacion?: string;
  fechaRecepcion?: string;
  estado?: 'PENDIENTE' | 'RECIBIDA';
  total: number;
}

export interface DetalleOrdenCompra {
  id?: number;
  idOrden?: number;
  idProducto: number;
  cantidad: number;
  precioUnitario: number;
}

export interface CrearOrdenDto {
  orden: OrdenCompra;
  detalles: DetalleOrdenCompra[];
}

@Injectable({
  providedIn: 'root'
})
export class ProveedorService {
  private http = inject(HttpClient);
  private apiUrl = '/api/proveedores';

  // Suppliers CRUD
  getAllProveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(this.apiUrl);
  }

  getProveedorById(id: number): Observable<Proveedor> {
    return this.http.get<Proveedor>(`${this.apiUrl}/${id}`);
  }

  createProveedor(proveedor: Proveedor): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.apiUrl, proveedor);
  }

  updateProveedor(id: number, proveedor: Proveedor): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.apiUrl}/${id}`, proveedor);
  }

  deleteProveedor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Purchase Orders Mappings
  getAllOrdenes(): Observable<OrdenCompra[]> {
    return this.http.get<OrdenCompra[]>(`${this.apiUrl}/ordenes`);
  }

  getOrdenById(id: number): Observable<OrdenCompra> {
    return this.http.get<OrdenCompra>(`${this.apiUrl}/ordenes/${id}`);
  }

  getDetallesByOrden(id: number): Observable<DetalleOrdenCompra[]> {
    return this.http.get<DetalleOrdenCompra[]>(`${this.apiUrl}/ordenes/${id}/detalles`);
  }

  crearOrdenCompra(dto: CrearOrdenDto): Observable<OrdenCompra> {
    return this.http.post<OrdenCompra>(`${this.apiUrl}/ordenes`, dto);
  }

  recibirOrdenCompra(id: number): Observable<OrdenCompra> {
    return this.http.post<OrdenCompra>(`${this.apiUrl}/ordenes/${id}/recibir`, {});
  }
}

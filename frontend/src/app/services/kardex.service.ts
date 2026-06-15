import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Kardex {
  id?: number;
  idProducto: number;
  tipoMovimiento: 'ENTRADA' | 'SALIDA' | 'AJUSTE';
  cantidad: number;
  stockAnterior?: number;
  stockPosterior?: number;
  justificacion: string;
  idUsuario: number;
  fecha?: string;
}

@Injectable({
  providedIn: 'root'
})
export class KardexService {
  private http = inject(HttpClient);
  private apiUrl = '/api/kardex';

  getAllKardex(): Observable<Kardex[]> {
    return this.http.get<Kardex[]>(this.apiUrl);
  }

  getKardexByProduct(idProducto: number): Observable<Kardex[]> {
    return this.http.get<Kardex[]>(`${this.apiUrl}/producto/${idProducto}`);
  }

  registrarMovimiento(movimiento: Kardex): Observable<Kardex> {
    return this.http.post<Kardex>(`${this.apiUrl}/registrar`, movimiento);
  }
}

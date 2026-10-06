import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PreApproved } from '../models/pre-approved.model';

@Injectable({
  providedIn: 'root'
})
export class PreApprovedService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/v1/preapproved';

  //Consulta los pre aprobados por usuario
  getPreApprovedByCustomer(customerId: string): Observable<PreApproved[]> {
    return this.http.get<PreApproved[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  //Consulta todos los pre aprobados del banco
  getAllPreApproved(): Observable<PreApproved[]> {
    return this.http.get<PreApproved[]>(this.apiUrl);
  }
}

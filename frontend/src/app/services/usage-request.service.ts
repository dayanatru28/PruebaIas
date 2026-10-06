import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UsageRequestDto, UsageResponseDto } from '../models/usage-request.model';

@Injectable({
  providedIn: 'root'
})
export class UsageRequestService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/v1/requests';

  //Envio de la solicitud al backend
  processUsageRequest(request: UsageRequestDto): Observable<UsageResponseDto> {
    return this.http.post<UsageResponseDto>(this.apiUrl, request);
  }
  
  //Consulta las solciitudes recientes
  getRecentRequests(): Observable<UsageResponseDto[]> {
    return this.http.get<UsageResponseDto[]>(this.apiUrl);
  }

  //Busca una solicitud por codigo de referencia
  getRequestByReference(reference: string): Observable<UsageResponseDto> {
    return this.http.get<UsageResponseDto>(`${this.apiUrl}/${reference}`);
  }
}

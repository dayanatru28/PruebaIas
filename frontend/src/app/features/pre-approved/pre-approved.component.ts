import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PreApprovedService } from '../../services/pre-approved.service';
import { UsageRequestService } from '../../services/usage-request.service';
import { PreApproved } from '../../models/pre-approved.model';
import { UsageRequestDto, UsageResponseDto } from '../../models/usage-request.model';

@Component({
  selector: 'app-pre-approved',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pre-approved.component.html',
  styleUrls: ['./pre-approved.component.css']
})
export class PreApprovedComponent implements OnInit {

  // Inyección de servicios
  private preApprovedService = inject(PreApprovedService);
  private usageRequestService = inject(UsageRequestService);

  // Cliente inicial
  customerId: string = 'USR-10';
  preApprovedList: PreApproved[] = [];
  recentRequests: UsageResponseDto[] = [];
  
  // Formulario de nueva solicitud
  requestReference: string = 'REF-001';
  selectedPreApprovedId: string = 'PRA-1001';
  amount: number | null = 600000;

  // Estados visuales y resultados
  isLoading: boolean = false;
  lastResult: UsageResponseDto | null = null;
  errorMessage: string = '';

  // Búsqueda por referencia
  searchReference: string = '';
  searchResult: UsageResponseDto | null = null;
  searchError: string = '';

  ngOnInit(): void {
    this.refreshAll();
  }

  //Refresca las busquedas para el navegador
  refreshAll(): void {
    this.loadPreApproved();
    this.loadRecentRequests();
  }

  //Hace la busqueda por usuarios de los pre aprobados
  loadPreApproved(): void {
    this.preApprovedService.getPreApprovedByCustomer(this.customerId).subscribe({
      next: (data) => {
        this.preApprovedList = data;
        if (data.length > 0 && !data.some(p => p.id === this.selectedPreApprovedId)) {
          this.selectedPreApprovedId = data[0].id;
        }
      },
      error: (err) => {
        console.error('Error al cargar preaprobados:', err);
      }
    });
  }

  //hace la busqueda de las solicitudes recientes 
  loadRecentRequests(): void {
    this.usageRequestService.getRecentRequests().subscribe({
      next: (data) => {
        this.recentRequests = data;
      },
      error: (err) => {
        console.error('Error al cargar historial:', err);
      }
    });
  }

  //Crea y envia la solicitud del pre aprobado al backend
  submitRequest(): void {
    this.errorMessage = '';
    this.lastResult = null;

    if (!this.requestReference.trim()) {
      this.errorMessage = 'La referencia de la solicitud es obligatoria.';
      return;
    }
    if (!this.selectedPreApprovedId) {
      this.errorMessage = 'Debe seleccionar un cupo preaprobado.';
      return;
    }
    if (!this.amount || this.amount <= 0) {
      this.errorMessage = 'El monto solicitado debe ser mayor que cero.';
      return;
    }

    const payload: UsageRequestDto = {
      requestReference: this.requestReference.trim(),
      preApprovedId: this.selectedPreApprovedId,
      customerId: this.customerId,
      amount: this.amount
    };

    this.isLoading = true;
    this.usageRequestService.processUsageRequest(payload).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.lastResult = response;
        // Recargamos los saldos y el historial inmediatamente
        this.refreshAll();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error al procesar la solicitud:', err);
        if (err.error && err.error.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'No se pudo comunicar con el servidor backend.';
        }
      }
    });
  }

  //Actualiza a informacion del pre aprobado seleccionando la tarjeta
  selectCard(card: PreApproved): void {
    this.selectedPreApprovedId = card.id;
  }

  generateNewReference(): void {
    const randomNum = Math.floor(100 + Math.random() * 900);
    this.requestReference = `REF-${randomNum}`;
  }

  //Actualiza la informacion del usuario
  changeCustomer(newCustomer: string): void {
    this.customerId = newCustomer;
    this.lastResult = null;
    this.errorMessage = '';
    this.refreshAll();
  }

  onSearchByReference(): void {
    this.searchResult = null;
    this.searchError = '';

    if (!this.searchReference.trim()) {
      this.searchError = 'Ingrese una referencia para buscar.';
      return;
    }

    this.usageRequestService.getRequestByReference(this.searchReference.trim()).subscribe({
      next: (res) => {
        this.searchResult = res;
      },
      error: () => {
        this.searchError = `No se encontró ninguna solicitud con la referencia '${this.searchReference}'.`;
      }
    });
  }
}

import { Component } from '@angular/core';
import { PreApprovedComponent } from './features/pre-approved/pre-approved.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [PreApprovedComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'BancoIAS - Portal Financiero';
}

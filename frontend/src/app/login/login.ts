import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private authService = inject(AuthService);
  private router = inject(Router);

  nickname = '';
  password = '';

  errorMessage = signal<string | null>(null);
  isLoading = signal(false);

  onSubmit(): void {
    if (!this.nickname || !this.password) {
      this.errorMessage.set('Por favor, complete todos los campos.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.nickname, this.password).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/products']);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Usuario o contraseña incorrectos.');
        console.error(err);
      }
    });
  }
}

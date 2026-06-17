import { Component, OnInit, inject, signal, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductService, Producto } from '../services/product.service';
import { AuthService } from '../services/auth.service';
import { KardexService, Kardex } from '../services/kardex.service';
import { ProveedorService, Proveedor, OrdenCompra, DetalleOrdenCompra, CrearOrdenDto } from '../services/proveedor.service';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.html',
  styleUrl: './products.css'
})
export class Products implements OnInit {
  private productService = inject(ProductService);
  private authService = inject(AuthService);
  private kardexService = inject(KardexService);
  private proveedorService = inject(ProveedorService);
  private router = inject(Router);

  // Signals for Data Model
  products = signal<Producto[]>([]);
  kardexMovements = signal<Kardex[]>([]);
  proveedores = signal<Proveedor[]>([]);
  ordenesCompra = signal<OrdenCompra[]>([]);
  lowStockProducts = signal<Producto[]>([]);

  // UI State Signals
  activeTab = signal<'products' | 'kardex' | 'providers' | 'reports'>('products');
  activeProviderSubTab = signal<'list' | 'orders'>('list');
  isDarkMode = signal(false);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  // Authentication & Roles
  username = this.authService.currentUser;
  userRole = this.authService.currentUserRole;

  // Category Dictionary
  categories: { [key: number]: string } = {
    1: 'Niños',
    2: 'Niñas',
    3: 'Mujer',
    4: 'Hombre',
    5: 'Accesorios',
    6: 'Unisex'
  };

  // Modals state
  isModalOpen = signal(false);
  isEditMode = signal(false);
  isUploadingImage = signal(false);
  imageUploadError = signal<string | null>(null);
  isProductCameraOpen = signal(false);
  productCameraError = signal<string | null>(null);
  productCameraStream: MediaStream | null = null;
  selectedProduct: Producto = this.getEmptyProduct();

  isDeleteModalOpen = signal(false);
  productIdToDelete: number | null = null;

  // Kárdex Manual Adjustment Modal
  isKardexModalOpen = signal(false);
  selectedKardex: Kardex = this.getEmptyKardex();

  // Proveedores Modal
  isProveedorModalOpen = signal(false);
  isProveedorEditMode = signal(false);
  selectedProveedor: Proveedor = this.getEmptyProveedor();

  // Purchase Order Modal
  isOrderModalOpen = signal(false);
  selectedProviderId: number | null = null;
  orderItems: { idProducto: number; cantidad: number; precioUnitario: number }[] = [];

  // View Order Details Modal
  isOrderDetailsModalOpen = signal(false);
  selectedOrder: OrdenCompra | null = null;
  selectedOrderDetails: DetalleOrdenCompra[] = [];

  // Barcode Scanner state
  isScannerOpen = signal(false);
  scannerErrorMessage = signal<string | null>(null);
  scannerMode = signal<'barcode_search' | 'product_form' | 'kardex_form'>('barcode_search');
  barcodeSearchQuery = signal('');
  html5QrcodeScanner: any = null;

  ngOnInit(): void {
    // Check local storage theme
    const theme = localStorage.getItem('theme');
    if (theme === 'dark') {
      this.isDarkMode.set(true);
      document.documentElement.classList.add('dark-mode');
      document.body.classList.add('dark-mode');
    } else {
      this.isDarkMode.set(false);
      document.documentElement.classList.remove('dark-mode');
      document.body.classList.remove('dark-mode');
    }

    this.loadAllData();
  }

  loadAllData(): void {
    this.loadProducts();
    this.loadKardex();
    this.loadProveedores();
    this.loadOrdenesCompra();
    this.loadLowStockAlerts();
  }

  // ---- 1. Catalogo / Products ----
  loadProducts(): void {
    this.isLoading.set(true);
    this.productService.getAllProducts().subscribe({
      next: (data) => {
        this.products.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Error al cargar la lista de productos.');
        console.error(err);
      }
    });
  }

  loadLowStockAlerts(): void {
    this.productService.getLowStockProducts().subscribe({
      next: (data) => {
        this.lowStockProducts.set(data);
      },
      error: (err) => {
        console.error('Error al cargar alertas de stock:', err);
      }
    });
  }

  getEmptyProduct(): Producto {
    return {
      nombre: '',
      descripcion: '',
      precio: 0.0,
      stock: 0,
      idCategoria: 5,
      stockMinimo: 5,
      codigoBarras: '',
      imagenUrl: ''
    };
  }

  openCreateModal(): void {
    this.isEditMode.set(false);
    this.imageUploadError.set(null);
    this.selectedProduct = this.getEmptyProduct();
    this.isModalOpen.set(true);
  }

  openEditModal(product: Producto): void {
    this.isEditMode.set(true);
    this.imageUploadError.set(null);
    this.selectedProduct = { ...product };
    this.isModalOpen.set(true);
  }

  closeModal(): void {
    this.closeProductCamera();
    this.isModalOpen.set(false);
    this.imageUploadError.set(null);
  }

  onProductImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;

    if (!['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(file.type)) {
      this.imageUploadError.set('Formato no permitido. Use JPG, PNG, WEBP o GIF.');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.imageUploadError.set('La imagen no puede superar los 5 MB.');
      return;
    }

    this.uploadProductImage(file);
  }

  private uploadProductImage(file: File): void {
    this.imageUploadError.set(null);
    this.isUploadingImage.set(true);
    this.productService.uploadProductImage(file).subscribe({
      next: (response) => {
        this.selectedProduct.imagenUrl = this.productService.getPublicImageUrl(response.url);
        this.isUploadingImage.set(false);
      },
      error: (err) => {
        this.isUploadingImage.set(false);
        this.imageUploadError.set(this.getErrorMessage(err, 'No se pudo subir la imagen.'));
      }
    });
  }

  openProductCamera(): void {
    this.closeProductCamera();
    this.productCameraError.set(null);
    this.isProductCameraOpen.set(true);

    setTimeout(async () => {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: { ideal: 'environment' } },
          audio: false
        });
        if (!this.isProductCameraOpen()) {
          stream.getTracks().forEach(track => track.stop());
          return;
        }
        this.productCameraStream = stream;
        const video = document.getElementById('product-camera-video') as HTMLVideoElement | null;
        if (video) {
          video.srcObject = this.productCameraStream;
          await video.play();
        }
      } catch {
        this.productCameraError.set('No se pudo acceder a la cámara. Revise los permisos del navegador.');
      }
    });
  }

  captureProductPhoto(): void {
    const video = document.getElementById('product-camera-video') as HTMLVideoElement | null;
    if (!video || !video.videoWidth || !video.videoHeight) {
      this.productCameraError.set('La cámara todavía no está lista.');
      return;
    }

    const maxDimension = 1600;
    const scale = Math.min(1, maxDimension / Math.max(video.videoWidth, video.videoHeight));
    const canvas = document.createElement('canvas');
    canvas.width = Math.round(video.videoWidth * scale);
    canvas.height = Math.round(video.videoHeight * scale);
    canvas.getContext('2d')?.drawImage(video, 0, 0, canvas.width, canvas.height);
    canvas.toBlob(blob => {
      if (!blob) {
        this.productCameraError.set('No se pudo capturar la foto.');
        return;
      }
      this.closeProductCamera();
      this.uploadProductImage(new File([blob], `producto-${Date.now()}.jpg`, { type: 'image/jpeg' }));
    }, 'image/jpeg', 0.88);
  }

  closeProductCamera(): void {
    this.productCameraStream?.getTracks().forEach(track => track.stop());
    this.productCameraStream = null;
    this.isProductCameraOpen.set(false);
    this.productCameraError.set(null);
  }

  removeProductImage(): void {
    this.selectedProduct.imagenUrl = '';
    this.imageUploadError.set(null);
  }

  saveProduct(): void {
    if (!this.selectedProduct.nombre || this.selectedProduct.precio <= 0 || this.selectedProduct.stock < 0) {
      this.errorMessage.set('Por favor, valide los campos del producto.');
      return;
    }

    this.isLoading.set(true);
    
    if (this.isEditMode() && this.selectedProduct.id !== undefined) {
      this.productService.updateProduct(this.selectedProduct.id, this.selectedProduct).subscribe({
        next: () => {
          this.loadProducts();
          this.loadLowStockAlerts();
          this.closeModal();
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(this.getErrorMessage(err, 'Fallo al actualizar el producto.'));
          console.error(err);
        }
      });
    } else {
      this.productService.createProduct(this.selectedProduct).subscribe({
        next: () => {
          this.loadProducts();
          this.loadLowStockAlerts();
          this.closeModal();
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(this.getErrorMessage(err, 'Fallo al crear el producto.'));
          console.error(err);
        }
      });
    }
  }

  openDeleteModal(id: number): void {
    this.productIdToDelete = id;
    this.isDeleteModalOpen.set(true);
  }

  closeDeleteModal(): void {
    this.productIdToDelete = null;
    this.isDeleteModalOpen.set(false);
  }

  confirmDelete(): void {
    if (this.productIdToDelete === null) return;

    this.isLoading.set(true);
    this.productService.deleteProduct(this.productIdToDelete).subscribe({
      next: () => {
        this.loadProducts();
        this.loadLowStockAlerts();
        this.closeDeleteModal();
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Fallo al eliminar el producto. Asegúrese de que no tenga movimientos o referencias.');
        console.error(err);
      }
    });
  }

  // ---- 2. Kárdex Panel ----
  loadKardex(): void {
    this.kardexService.getAllKardex().subscribe({
      next: (data) => {
        this.kardexMovements.set(data);
      },
      error: (err) => {
        console.error('Error al cargar movimientos de kárdex:', err);
      }
    });
  }

  getEmptyKardex(): Kardex {
    return {
      idProducto: 0,
      tipoMovimiento: 'ENTRADA',
      cantidad: 1,
      justificacion: '',
      idUsuario: this.authService.getUserId()
    };
  }

  openKardexModal(): void {
    if (this.products().length === 0) {
      alert('Debe tener al menos un producto en el catálogo para registrar movimientos.');
      return;
    }
    this.selectedKardex = this.getEmptyKardex();
    // Default to first product
    this.selectedKardex.idProducto = this.products()[0].id || 0;
    this.isKardexModalOpen.set(true);
  }

  closeKardexModal(): void {
    this.isKardexModalOpen.set(false);
  }

  saveKardexMovement(): void {
    const invalidQuantity = this.selectedKardex.tipoMovimiento === 'AJUSTE'
      ? this.selectedKardex.cantidad < 0
      : this.selectedKardex.cantidad <= 0;
    if (invalidQuantity || !this.selectedKardex.justificacion) {
      alert('Por favor complete la justificación y una cantidad válida.');
      return;
    }
    this.isLoading.set(true);
    this.selectedKardex.idUsuario = this.authService.getUserId();
    
    this.kardexService.registrarMovimiento(this.selectedKardex).subscribe({
      next: () => {
        this.loadProducts();
        this.loadKardex();
        this.loadLowStockAlerts();
        this.closeKardexModal();
      },
      error: (err) => {
        this.isLoading.set(false);
        alert('Error al registrar movimiento: ' + (err.error || err.message));
        console.error(err);
      }
    });
  }

  filterKardexByProduct(event: any): void {
    const productIdStr = event.target.value;
    if (!productIdStr) {
      this.loadKardex();
    } else {
      const prodId = parseInt(productIdStr);
      this.kardexService.getKardexByProduct(prodId).subscribe({
        next: (data) => this.kardexMovements.set(data),
        error: (err) => console.error(err)
      });
    }
  }

  // ---- 3. Proveedores & Órdenes ----
  loadProveedores(): void {
    this.proveedorService.getAllProveedores().subscribe({
      next: (data) => this.proveedores.set(data),
      error: (err) => console.error(err)
    });
  }

  loadOrdenesCompra(): void {
    this.proveedorService.getAllOrdenes().subscribe({
      next: (data) => this.ordenesCompra.set(data),
      error: (err) => console.error(err)
    });
  }

  getEmptyProveedor(): Proveedor {
    return {
      nombre: '',
      ruc: '',
      direccion: '',
      telefono: '',
      email: ''
    };
  }

  openProveedorCreateModal(): void {
    this.isProveedorEditMode.set(false);
    this.selectedProveedor = this.getEmptyProveedor();
    this.isProveedorModalOpen.set(true);
  }

  openProveedorEditModal(prov: Proveedor): void {
    this.isProveedorEditMode.set(true);
    this.selectedProveedor = { ...prov };
    this.isProveedorModalOpen.set(true);
  }

  closeProveedorModal(): void {
    this.isProveedorModalOpen.set(false);
  }

  saveProveedor(): void {
    if (!this.selectedProveedor.nombre || !this.selectedProveedor.ruc || this.selectedProveedor.ruc.length !== 11) {
      alert('El RUC debe tener 11 caracteres y el nombre es requerido.');
      return;
    }
    this.isLoading.set(true);

    if (this.isProveedorEditMode() && this.selectedProveedor.id !== undefined) {
      this.proveedorService.updateProveedor(this.selectedProveedor.id, this.selectedProveedor).subscribe({
        next: () => {
          this.loadProveedores();
          this.isLoading.set(false);
          this.closeProveedorModal();
        },
        error: (err) => {
          this.isLoading.set(false);
          alert('Error al actualizar proveedor.');
        }
      });
    } else {
      this.proveedorService.createProveedor(this.selectedProveedor).subscribe({
        next: () => {
          this.loadProveedores();
          this.isLoading.set(false);
          this.closeProveedorModal();
        },
        error: (err) => {
          this.isLoading.set(false);
          alert('RUC duplicado o error al registrar proveedor.');
        }
      });
    }
  }

  deleteProveedor(id: number): void {
    if (confirm('¿Está seguro de que desea eliminar este proveedor?')) {
      this.proveedorService.deleteProveedor(id).subscribe({
        next: () => this.loadProveedores(),
        error: (e) => alert('Fallo al eliminar proveedor. Asegúrese de que no tenga órdenes registradas.')
      });
    }
  }

  // Purchase Order Operations
  openOrderCreateModal(): void {
    if (this.proveedores().length === 0) {
      alert('Debe registrar al menos un proveedor antes de crear órdenes de compra.');
      return;
    }
    this.selectedProviderId = this.proveedores()[0].id || null;
    this.orderItems = [{ idProducto: this.products()[0]?.id || 0, cantidad: 10, precioUnitario: 20 }];
    this.isOrderModalOpen.set(true);
  }

  closeOrderModal(): void {
    this.isOrderModalOpen.set(false);
  }

  addOrderItem(): void {
    this.orderItems.push({ idProducto: this.products()[0]?.id || 0, cantidad: 10, precioUnitario: 20 });
  }

  removeOrderItem(index: number): void {
    this.orderItems.splice(index, 1);
  }

  calculateOrderTotal(): number {
    return this.orderItems.reduce((acc, item) => acc + (item.cantidad * item.precioUnitario), 0);
  }

  savePurchaseOrder(): void {
    if (!this.selectedProviderId) return;
    if (this.orderItems.length === 0) {
      alert('Agregue al menos un producto a la orden.');
      return;
    }

    this.isLoading.set(true);
    const total = this.calculateOrderTotal();

    const dto: CrearOrdenDto = {
      orden: {
        idProveedor: this.selectedProviderId,
        total: total
      },
      detalles: this.orderItems.map(item => ({
        idProducto: item.idProducto,
        cantidad: item.cantidad,
        precioUnitario: item.precioUnitario
      }))
    };

    this.proveedorService.crearOrdenCompra(dto).subscribe({
      next: () => {
        this.loadOrdenesCompra();
        this.isLoading.set(false);
        this.closeOrderModal();
      },
      error: (err) => {
        this.isLoading.set(false);
        alert('Fallo al crear la orden de compra: ' + err.message);
      }
    });
  }

  viewOrderDetails(order: OrdenCompra): void {
    if (order.id === undefined) return;
    this.selectedOrder = order;
    this.isLoading.set(true);
    this.proveedorService.getDetallesByOrden(order.id).subscribe({
      next: (data) => {
        this.selectedOrderDetails = data;
        this.isLoading.set(false);
        this.isOrderDetailsModalOpen.set(true);
      },
      error: (e) => {
        this.isLoading.set(false);
        alert('Error al cargar los detalles de la orden.');
      }
    });
  }

  closeOrderDetailsModal(): void {
    this.isOrderDetailsModalOpen.set(false);
    this.selectedOrder = null;
    this.selectedOrderDetails = [];
  }

  receiveOrder(id: number): void {
    this.isLoading.set(true);
    const userId = this.authService.getUserId();
    this.proveedorService.recibirOrdenCompra(id, userId).subscribe({
      next: () => {
        this.loadProducts();
        this.loadKardex();
        this.loadOrdenesCompra();
        this.loadLowStockAlerts();
        this.isLoading.set(false);
        alert('¡Orden de compra recibida con éxito! Inventario actualizado.');
      },
      error: (err) => {
        this.isLoading.set(false);
        alert('Error al recibir la orden de compra: ' + (err.error || err.message));
      }
    });
  }

  // ---- 4. Reportes Panel ----
  calculateValuation(): number {
    return this.products().reduce((acc, p) => acc + (p.stock * p.precio), 0);
  }

  calculateTotalUnits(): number {
    return this.products().reduce((acc, p) => acc + p.stock, 0);
  }

  private getErrorMessage(error: any, fallback: string): string {
    if (typeof error?.error === 'string' && error.error.trim()) {
      return error.error;
    }
    return error?.error?.message || fallback;
  }

  // ---- 5. Barcode Scanner Controls ----
  openScanner(mode: 'barcode_search' | 'product_form' | 'kardex_form'): void {
    this.scannerMode.set(mode);
    this.isScannerOpen.set(true);
    this.scannerErrorMessage.set(null);
    this.startCameraScanner();
  }

  closeScanner(): void {
    this.stopCameraScanner();
    this.isScannerOpen.set(false);
  }

  startCameraScanner(): void {
    setTimeout(() => {
      try {
        const Html5Qrcode = (window as any).Html5Qrcode;
        if (!Html5Qrcode) {
          this.scannerErrorMessage.set('Librería de escaneo no disponible.');
          return;
        }

        this.html5QrcodeScanner = new Html5Qrcode("scanner-reader");
        this.html5QrcodeScanner.start(
          { facingMode: "environment" },
          {
            fps: 15,
            qrbox: { width: 250, height: 250 }
          },
          (decodedText: string) => {
            this.handleScannedCode(decodedText);
          },
          (errorMessage: string) => {
            // Keep scanning, silent errors
          }
        ).catch((err: any) => {
          this.scannerErrorMessage.set('Error al acceder a la cámara. Verifique los permisos.');
          console.error(err);
        });
      } catch (e: any) {
        this.scannerErrorMessage.set('Error al inicializar cámara: ' + e.message);
      }
    }, 400);
  }

  stopCameraScanner(): void {
    if (this.html5QrcodeScanner) {
      this.html5QrcodeScanner.stop().then(() => {
        this.html5QrcodeScanner = null;
      }).catch((err: any) => {
        console.error('Fallo al detener la cámara:', err);
        this.html5QrcodeScanner = null;
      });
    }
  }

  handleScannedCode(code: string): void {
    this.stopCameraScanner();
    this.isScannerOpen.set(false);

    if (this.scannerMode() === 'barcode_search') {
      this.barcodeSearchQuery.set(code);
      this.searchByBarcode(code);
    } else if (this.scannerMode() === 'product_form') {
      this.selectedProduct.codigoBarras = code;
    } else if (this.scannerMode() === 'kardex_form') {
      // Find product matching barcode
      const prod = this.products().find(p => p.codigoBarras === code);
      if (prod && prod.id) {
        this.selectedKardex.idProducto = prod.id;
      } else {
        alert('Producto con código escaneado no encontrado en catálogo.');
      }
    }
  }

  searchByBarcode(code: string): void {
    if (!code) {
      this.loadProducts();
      return;
    }
    this.isLoading.set(true);
    this.productService.getProductByBarcode(code).subscribe({
      next: (prod) => {
        this.isLoading.set(false);
        if (prod) {
          this.products.set([prod]);
        } else {
          this.products.set([]);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.products.set([]);
        this.errorMessage.set('Código de barras no registrado en ningún producto.');
        setTimeout(() => this.errorMessage.set(null), 4000);
      }
    });
  }

  clearBarcodeSearch(): void {
    this.barcodeSearchQuery.set('');
    this.loadProducts();
  }

  // ---- 6. Excel Import & Export ----
  exportToExcel(): void {
    const exportedAt = new Date();
    const exportedAtText = this.formatDateTime(exportedAt);
    const exportData = this.products().map(p => ({
      ID: p.id,
      Nombre: p.nombre,
      Descripción: p.descripcion,
      Precio: p.precio,
      Stock: p.stock,
      'Stock Mínimo': p.stockMinimo || 5,
      'Código de Barras': p.codigoBarras || '',
      'URL Imagen': p.imagenUrl || '',
      Categoría: this.getCategoryName(p.idCategoria),
      'Exportado en': exportedAtText
    }));

    const summaryData = [
      { Campo: 'Exportado en', Valor: exportedAtText },
      { Campo: 'Total productos', Valor: this.products().length },
      { Campo: 'Usuario', Valor: this.username() || '-' }
    ];

    const ws = XLSX.utils.json_to_sheet(exportData);
    const summaryWs = XLSX.utils.json_to_sheet(summaryData);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, summaryWs, 'Resumen');
    XLSX.utils.book_append_sheet(wb, ws, 'Productos');
    XLSX.writeFile(wb, `Catalogo_BeautifulSkin_Inventario_${this.formatDateTimeForFile(exportedAt)}.xlsx`);
  }

  importFromExcel(event: any): void {
    const target = event.target as HTMLInputElement;
    const files = target.files;
    if (!files || files.length === 0) return;

    const file = files[0];
    const reader = new FileReader();

    reader.onload = (e: any) => {
      const data = new Uint8Array(e.target.result);
      const workbook = XLSX.read(data, { type: 'array' });
      const sheetName = workbook.SheetNames[0];
      const worksheet = workbook.Sheets[sheetName];
      const rows = XLSX.utils.sheet_to_json(worksheet) as any[];

      let importedCount = 0;
      let errorCount = 0;
      this.isLoading.set(true);

      const observables = [];
      for (const row of rows) {
        const prod: Producto = {
          nombre: row['Nombre'] || row['nombre'],
          descripcion: row['Descripción'] || row['descripcion'] || '',
          precio: parseFloat(row['Precio'] || row['precio'] || '0.0'),
          stock: parseInt(row['Stock'] || row['stock'] || '0'),
          idCategoria: this.parseCategoryFromExcel(row),
          stockMinimo: parseInt(row['Stock Mínimo'] || row['stock_minimo'] || '5'),
          codigoBarras: row['Código de Barras'] || row['codigo_barras'] ? String(row['Código de Barras'] || row['codigo_barras']) : '',
          imagenUrl: row['URL Imagen'] || row['imagen_url'] || ''
        };

        if (prod.nombre && prod.precio > 0 && prod.stock >= 0) {
          observables.push(this.productService.createProduct(prod));
        } else {
          errorCount++;
        }
      }

      if (observables.length === 0) {
        this.isLoading.set(false);
        alert(`No se encontraron filas con datos válidos para importar. Errores: ${errorCount}`);
        return;
      }

      let completed = 0;
      let successCount = 0;

      observables.forEach(obs => {
        obs.subscribe({
          next: () => {
            completed++;
            successCount++;
            if (completed === observables.length) {
              this.loadProducts();
              this.loadKardex();
              this.loadLowStockAlerts();
              this.isLoading.set(false);
              alert(`Importación finalizada. Éxitos: ${successCount}, Ignorados/Errores: ${errorCount}`);
            }
          },
          error: () => {
            completed++;
            errorCount++;
            if (completed === observables.length) {
              this.loadProducts();
              this.loadKardex();
              this.loadLowStockAlerts();
              this.isLoading.set(false);
              alert(`Importación finalizada con algunos fallos. Éxitos: ${successCount}, Errores/Ignorados: ${errorCount}`);
            }
          }
        });
      });
    };

    reader.readAsArrayBuffer(file);
    target.value = ''; // Reset input
  }

  // ---- Helper UI Methods ----
  getCategoryName(id: number): string {
    return this.categories[id] || 'Accesorios';
  }

  private parseCategoryFromExcel(row: any): number {
    const rawId = row['Categoría ID'] || row['categoria_id'];
    if (rawId) {
      return parseInt(rawId, 10);
    }
    const rawName = String(row['Categoría'] || row['categoria'] || '').trim().toLowerCase();
    const found = Object.entries(this.categories)
      .find(([, name]) => name.toLowerCase() === rawName);
    return found ? Number(found[0]) : 5;
  }

  private formatDateTime(date: Date): string {
    return new Intl.DateTimeFormat('es-PE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).format(date);
  }

  private formatDateTimeForFile(date: Date): string {
    const pad = (value: number) => value.toString().padStart(2, '0');
    return `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}_${pad(date.getHours())}${pad(date.getMinutes())}${pad(date.getSeconds())}`;
  }

  getProviderName(id: number): string {
    const prov = this.proveedores().find(p => p.id === id);
    return prov ? prov.nombre : `Proveedor #${id}`;
  }

  getProductName(id: number): string {
    const prod = this.products().find(p => p.id === id);
    return prod ? prod.nombre : `Producto #${id}`;
  }

  toggleTheme(): void {
    const nextMode = !this.isDarkMode();
    this.isDarkMode.set(nextMode);
    if (nextMode) {
      document.documentElement.classList.add('dark-mode');
      document.body.classList.add('dark-mode');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark-mode');
      document.body.classList.remove('dark-mode');
      localStorage.setItem('theme', 'light');
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

export interface UsageRequestDto {
  requestReference: string;
  preApprovedId: string;
  customerId: string;
  amount: number;
}

export interface UsageResponseDto {
  requestReference: string;
  preApprovedId: string;
  customerId: string;
  amount: number;
  status: 'AUTHORIZED' | 'REJECTED';
  rejectionReason: string | null;
  processedAt: string;
}

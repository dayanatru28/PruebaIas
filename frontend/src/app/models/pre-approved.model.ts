export interface PreApproved {
  id: string;
  customerId: string;
  status: 'ACTIVE' | 'BLOCKED';
  availableAmount: number;
}

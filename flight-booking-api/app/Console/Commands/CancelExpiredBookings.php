<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use App\Models\DatVe;
use App\Models\HanhKhach;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Log;
use Carbon\Carbon;

class CancelExpiredBookings extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'bookings:cancel-expired';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Tự động hủy các đặt vé đã hết hạn giữ chỗ và gửi email thông báo';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Bắt đầu kiểm tra và hủy các đặt vé hết hạn...');

        // Lấy các đặt vé đã hết hạn giữ chỗ nhưng chưa thanh toán
        $expiredBookings = DatVe::whereIn('trang_thai', ['giu_cho', 'cho_thanh_toan', 'chờ_thanh_toan'])
            ->whereNotNull('thoi_gian_het_han_giu_cho')
            ->where('thoi_gian_het_han_giu_cho', '<', now())
            ->with(['khach_hang', 'chuyen_bay.hang_hang_khong', 'chuyen_bay.tuyen_bay.san_bay_di', 'chuyen_bay.tuyen_bay.san_bay_den'])
            ->get();

        $count = 0;

        foreach ($expiredBookings as $booking) {
            // Kiểm tra xem đã đến giờ bay chưa
            // Chỉ hủy nếu chưa đến giờ bay (nếu đã đến giờ bay thì không hủy, để người dùng có thể thanh toán)
            $chuyenBay = $booking->chuyen_bay;
            if ($chuyenBay && $chuyenBay->gio_khoi_hanh) {
                // Nếu đã quá giờ bay, bỏ qua (không hủy)
                if (now() >= $chuyenBay->gio_khoi_hanh) {
                    continue;
                }
            }

            try {
                // Lấy thông tin liên hệ từ booking hoặc khách hàng
                $email = $booking->khach_hang->email ?? null;
                $tenDayDu = $booking->khach_hang->ten_day_du ?? $booking->khach_hang->email ?? 'Khách hàng';

                // Xóa hành khách
                HanhKhach::where('ma_dat_ve', $booking->id)->delete();

                // Cập nhật trạng thái
                $booking->update(['trang_thai' => 'da_huy']);

                // Gửi email thông báo hủy vé
                if ($email) {
                    try {
                        Mail::to($email)->send(new \App\Mail\BookingExpiredMail($booking, $tenDayDu));
                        Log::info('Expired booking cancellation email sent', [
                            'dat_ve_id' => $booking->id,
                            'email' => $email
                        ]);
                    } catch (\Exception $e) {
                        Log::error('Failed to send expired booking cancellation email', [
                            'dat_ve_id' => $booking->id,
                            'email' => $email,
                            'error' => $e->getMessage()
                        ]);
                    }
                }

                $count++;
                $this->info("Đã hủy đặt vé: {$booking->ma_dat_ve} (ID: {$booking->id})");
            } catch (\Exception $e) {
                Log::error('Error cancelling expired booking', [
                    'dat_ve_id' => $booking->id,
                    'error' => $e->getMessage()
                ]);
                $this->error("Lỗi khi hủy đặt vé {$booking->ma_dat_ve}: " . $e->getMessage());
            }
        }

        $this->info("Hoàn thành! Đã hủy {$count} đặt vé hết hạn.");
        return 0;
    }
}

<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Queue\SerializesModels;
use App\Models\DatVe;

class BookingExpiredMail extends Mailable
{
    use Queueable, SerializesModels;

    public $datVe;
    public $tenDayDu;

    /**
     * Create a new message instance.
     */
    public function __construct(DatVe $datVe, $tenDayDu = null)
    {
        $this->datVe = $datVe;
        $this->tenDayDu = $tenDayDu;
    }

    /**
     * Build the message.
     */
    public function build()
    {
        return $this->subject('Thông báo hủy đặt vé do hết hạn thanh toán')
            ->markdown('emails.booking-expired')
            ->with([
                'datVe' => $this->datVe,
                'tenDayDu' => $this->tenDayDu,
            ]);
    }
}


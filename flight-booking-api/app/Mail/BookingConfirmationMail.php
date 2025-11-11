<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;
use App\Models\DatVe;

class BookingConfirmationMail extends Mailable
{
    use Queueable, SerializesModels;

    public $datVe;
    public $thongTinLienHe;

    /**
     * Create a new message instance.
     */
    public function __construct(DatVe $datVe, $thongTinLienHe)
    {
        $this->datVe = $datVe;
        $this->thongTinLienHe = $thongTinLienHe;
    }

    /**
     * Get the message envelope.
     */
    public function envelope(): Envelope
    {
        return new Envelope(
            subject: 'Xác nhận đặt vé thành công - Mã đặt vé: ' . $this->datVe->ma_dat_ve,
        );
    }

    /**
     * Get the message content definition.
     */
    public function content(): Content
    {
        return new Content(
            markdown: 'emails.booking-confirmation',
            with: [
                'datVe' => $this->datVe,
                'thongTinLienHe' => $this->thongTinLienHe,
            ],
        );
    }

    /**
     * Get the attachments for the message.
     *
     * @return array<int, \Illuminate\Mail\Mailables\Attachment>
     */
    public function attachments(): array
    {
        return [];
    }
}

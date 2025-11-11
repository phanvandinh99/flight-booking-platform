<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;
use App\Models\ChuyenBay;
use App\Models\DatVe;

class FlightDelayMail extends Mailable
{
    use Queueable, SerializesModels;

    public $chuyenBay;
    public $datVe;
    public $thoiGianKhoiHanhMoi;

    /**
     * Create a new message instance.
     */
    public function __construct(ChuyenBay $chuyenBay, DatVe $datVe, $thoiGianKhoiHanhMoi = null)
    {
        $this->chuyenBay = $chuyenBay;
        $this->datVe = $datVe;
        $this->thoiGianKhoiHanhMoi = $thoiGianKhoiHanhMoi ?? $chuyenBay->gio_khoi_hanh;
    }

    /**
     * Get the message envelope.
     */
    public function envelope(): Envelope
    {
        return new Envelope(
            subject: 'Thông báo hoãn chuyến bay - ' . $this->chuyenBay->ma_chuyen_bay,
        );
    }

    /**
     * Get the message content definition.
     */
    public function content(): Content
    {
        return new Content(
            markdown: 'emails.flight-delay',
            with: [
                'chuyenBay' => $this->chuyenBay,
                'datVe' => $this->datVe,
                'thoiGianKhoiHanhMoi' => $this->thoiGianKhoiHanhMoi,
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

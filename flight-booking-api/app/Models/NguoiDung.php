<?php

namespace App\Models;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class NguoiDung extends Authenticatable
{
    use HasApiTokens, Notifiable;

    protected $table = 'nguoi_dung';
    protected $fillable = [
        'ten_day_du',
        'email',
        'so_dien_thoai',
        'mat_khau',
        'vai_tro',
        'ma_hang_hang_khong',
        'da_xac_thuc_email',
    ];
    protected $hidden = ['mat_khau', 'remember_token'];
    protected $casts = [
        'da_xac_thuc_email' => 'datetime',
        'email_verified_at' => 'datetime',
    ];

    // Quan hệ
    public function hang_hang_khong()
    {
        return $this->belongsTo(HangHangKhong::class, 'ma_hang_hang_khong');
    }

    public function dat_ve()
    {
        return $this->hasMany(DatVe::class, 'ma_khach_hang');
    }
}

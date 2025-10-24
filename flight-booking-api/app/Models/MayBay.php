<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class MayBay extends Model
{
    protected $table = 'may_bay';
    protected $fillable = [
        'ma_hang_hang_khong',
        'loai_may_bay',
        'tong_so_ghe',
        'so_do_ghe'
    ];
    protected $casts = ['so_do_ghe' => 'array']; // tự động chuyển JSON ↔ array

    public function hang_hang_khong()
    {
        return $this->belongsTo(HangHangKhong::class, 'ma_hang_hang_khong');
    }

    public function chuyen_bay()
    {
        return $this->hasMany(ChuyenBay::class, 'ma_may_bay');
    }
}

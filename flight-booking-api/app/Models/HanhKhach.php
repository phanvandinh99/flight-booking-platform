<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class HanhKhach extends Model
{
    protected $table = 'hanh_khach';
    protected $fillable = [
        'ma_dat_ve',
        'ho_ten',
        'so_ho_chieu',
        'so_ghe',
        'hang_ve'
    ];

    public function dat_ve()
    {
        return $this->belongsTo(DatVe::class, 'ma_dat_ve');
    }
}

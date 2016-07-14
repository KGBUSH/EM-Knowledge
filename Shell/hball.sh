#!/bin/bash
scan 'scrapy_baike_words_third',{COLUMNS=>['url']}
scan 'scrapy_baike_words_first',{COLUMNS=>['url']}
scan 'baike2_liutao_first',{COLUMNS=>['url']}         
scan 'scrapy_baike_words_second',{COLUMNS=>['url']}
scan 'baike_liutao',{COLUMNS=>['url']}
scan 'baike2_liutao_second',{COLUMNS=>['url']} 

import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, ViewChild } from '@angular/core';
import { TimelineMax, Power2, Power4 } from 'gsap';
import * as $ from 'jquery';
import { timestamp } from 'rxjs';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  posts: any = [];
  
  ngOnInit(): void {
    var obj = [{
      name: "Daan",
      tag: "daanoliveira",
      timestamp: Date.now(),
      textPost: `Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                                          Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
                                          eiusmod
                                          tempor incididunt ut labore et dolore magna aliqua.`,
      hashtag: "hashtag"
    },
    {
      name: "Cibelly",
      tag: "cibellymei",
      timestamp: Date.now(),
      textPost: `Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                                          Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
                                          eiusmod
                                          tempor incididunt ut labore et dolore magna aliqua.`,
      hashtag: "hashtag"
    },
    {
      name: "Luna",
      tag: "lunacat",
      timestamp: Date.now(),
      textPost: `Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                                          Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
                                          eiusmod
                                          tempor incididunt ut labore et dolore magna aliqua.`,
      hashtag: "hashtag"
    },
    {
      name: "Daan",
      tag: "daanoliveira",
      timestamp: Date.now(),
      textPost: `Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                                          Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
                                          eiusmod
                                          tempor incididunt ut labore et dolore magna aliqua.`,
      hashtag: "hashtag"
    },
    {
      name: "Daan",
      tag: "daanoliveira",
      timestamp: Date.now(),
      textPost: `Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                                          Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
                                          eiusmod
                                          tempor incididunt ut labore et dolore magna aliqua.`,
      hashtag: "hashtag"
    }]

    this.posts = obj
  }
}